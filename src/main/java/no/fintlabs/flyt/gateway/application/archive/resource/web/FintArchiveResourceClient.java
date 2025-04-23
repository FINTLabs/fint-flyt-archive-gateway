package no.fintlabs.flyt.gateway.application.archive.resource.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.AbstractCollectionResources;
import no.fint.model.resource.arkiv.noark.SakResource;
import no.fint.model.resource.arkiv.noark.SakResources;
import no.fintlabs.flyt.gateway.application.archive.WebUtilErrorHandler;
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

    private final Timer lastUpdatedTimer;
    private final Timer findCasesTimer;
    private final MeterRegistry meterRegistry;

    private final WebUtilErrorHandler webUtilErrorHandler;

    public FintArchiveResourceClient(
            WebClient fintWebClient,
            MeterRegistry meterRegistry,
            WebUtilErrorHandler webUtilErrorHandler
    ) {
        this.fintWebClient = fintWebClient;
        this.webUtilErrorHandler = webUtilErrorHandler;
        this.objectMapper = new ObjectMapper();
        this.meterRegistry = meterRegistry;

        this.lastUpdatedTimer = Timer.builder("fint.flyt.gateway.archive.resource.getResourcesLastUpdated")
                .description("Time to fetch and map last-updated resources")
                .register(meterRegistry);

        this.findCasesTimer = Timer.builder("fint.flyt.gateway.archive.resource.findCasesWithFilter")
                .description("Time to fetch cases with filter")
                .register(meterRegistry);
    }

    public <T> Mono<List<T>> getResourcesLastUpdated(String urlResourcePath, Class<T> resourceClass) {
        Timer.Sample sample = Timer.start(meterRegistry);
        return fintWebClient.get()
                .uri(urlResourcePath.concat("/last-updated"))
                .retrieve()
                .bodyToMono(LastUpdated.class)
                .flatMap(lastUpdated -> fintWebClient.get()
                        .uri(urlResourcePath, uriBuilder ->
                                uriBuilder.queryParam("sinceTimeStamp",
                                        sinceTimestamp.getOrDefault(urlResourcePath, 0L)).build())
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<ResourceCollection>() {
                        })
                        .flatMapIterable(AbstractCollectionResources::getContent)
                        .map(resource -> objectMapper.convertValue(resource, resourceClass))
                        .collectList()
                        .doOnNext(list -> sinceTimestamp.put(urlResourcePath, lastUpdated.getLastUpdated()))
                )
                .doOnError(webUtilErrorHandler::logAndSendError)
                .doFinally(signal -> sample.stop(lastUpdatedTimer));
    }

    public void resetLastUpdatedTimestamps() {
        this.sinceTimestamp.clear();
    }

    public Mono<List<SakResource>> findCasesWithFilter(String caseFilter) {
        Timer.Sample sample = Timer.start(meterRegistry);
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
                .doOnError(webUtilErrorHandler::logAndSendError)
                .doFinally(signal -> sample.stop(findCasesTimer));
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