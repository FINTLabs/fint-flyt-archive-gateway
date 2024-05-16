package no.fintlabs.flyt.gateway.application.archive.resource.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.AbstractCollectionResources;
import no.fint.model.resource.arkiv.noark.SakResource;
import no.fint.model.resource.arkiv.noark.SakResources;
import no.fintlabs.flyt.gateway.application.archive.resource.model.ResourceCollection;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class FintArchiveResourceClient {
    private final WebClient fintWebClient;

    private final Map<String, Long> sinceTimestamp = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;

    public FintArchiveResourceClient(WebClient fintWebClient) {
        this.fintWebClient = fintWebClient;
        this.objectMapper = new ObjectMapper();
    }

    public <T> Mono<List<T>> getResourcesLastUpdated(String urlResourcePath, Class<T> resourceClass) {
        return fintWebClient.get()
                .uri(urlResourcePath.concat("/last-updated"))
                .retrieve()
                .bodyToMono(LastUpdated.class)
                .flatMap(lastUpdated -> fintWebClient.get()
                        .uri(urlResourcePath, uriBuilder -> uriBuilder.queryParam("sinceTimeStamp", sinceTimestamp.getOrDefault(urlResourcePath, 0L)).build())
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<ResourceCollection>() {
                        })
                        .flatMapIterable(AbstractCollectionResources::getContent)
                        .map(resource -> objectMapper.convertValue(resource, resourceClass))
                        .collectList()
                        .doOnNext(it -> sinceTimestamp.put(urlResourcePath, lastUpdated.getLastUpdated()))
                );
    }

    public void resetLastUpdatedTimestamps() {
        this.sinceTimestamp.clear();
    }

    public Mono<List<SakResource>> findCasesWithFilter(String caseFilter) {
        return fintWebClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/arkiv/noark/sak")
                        .queryParam("$filter", caseFilter)
                        .build()
                )
                .retrieve()
                .bodyToMono(SakResources.class)
                .map(SakResources::getContent)
                .onErrorReturn(WebClientResponseException.NotFound.class, List.of())
                .doOnError(e -> {
                    if (e instanceof WebClientResponseException) {
                        log.error("{} body={}", e, ((WebClientResponseException) e).getResponseBodyAsString());
                    } else {
                        log.error(e.toString());
                    }
                });
    }

    public <T> Mono<T> getResources(String endpoint, Class<T> clazz) {
        return fintWebClient.get()
                .uri(endpoint)
                .retrieve()
                .bodyToMono(clazz);
    }

    @Data
    private static class LastUpdated {
        private Long lastUpdated;
    }

    public Mono<SakResource> getCase(URI uri) {
        return fintWebClient
                .get()
                .uri(uri)
                .retrieve()
                .bodyToMono(SakResource.class);
    }
}