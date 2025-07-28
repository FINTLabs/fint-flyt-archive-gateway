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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClientRequest;
import reactor.util.retry.Retry;

import java.net.URI;
import java.time.Duration;
import java.util.List;

@Component
@Slf4j
public class FintArchiveResourceClient {
    private final FintArchiveResourceClientProperties properties;
    private final WebClient fintWebClient;

    private final ObjectMapper objectMapper;

    private final Timer lastUpdatedTimer;
    private final Timer findCasesTimer;
    private final MeterRegistry meterRegistry;

    private final WebUtilErrorHandler webUtilErrorHandler;

    public FintArchiveResourceClient(
            FintArchiveResourceClientProperties fintArchiveResourceClientProperties,
            @Qualifier("fintWebClient") WebClient fintWebClient,
            MeterRegistry meterRegistry,
            WebUtilErrorHandler webUtilErrorHandler
    ) {
        this.properties = fintArchiveResourceClientProperties;
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

    public Mono<Long> getLastUpdated(String urlResourcePath) {
        Timer.Sample sample = Timer.start(meterRegistry);
        return fintWebClient.get()
                .uri(urlResourcePath.concat("/last-updated"))
                .httpRequest(clientHttpRequest -> {
                    HttpClientRequest reactorRequest = clientHttpRequest.getNativeRequest();
                    reactorRequest.responseTimeout(Duration.ofMillis(
                            properties.getGetResourcesLastUpdatedTimeoutMillis()
                    ));
                })
                .retrieve()
                .bodyToMono(LastUpdated.class)
                .map(LastUpdated::getLastUpdated)
                .doFinally(signal -> sample.stop(lastUpdatedTimer));
    }

    public <T> Flux<T> getResourcesSince(String urlResourcePath, Class<T> resourceClass, long sinceTimestamp) {
        return fintWebClient.get()
                .uri(uriBuilder ->
                        uriBuilder.path(urlResourcePath)
                                .queryParam("sinceTimeStamp", sinceTimestamp)
                                .build()
                )
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ResourceCollection>() {
                })
                .flatMapIterable(AbstractCollectionResources::getContent)
                .map(resource -> objectMapper.convertValue(resource, resourceClass))
                .doOnError(e -> {
                    if (e instanceof WebClientResponseException ex) {
                        log.error("{} body={}", ex, ex.getResponseBodyAsString());
                    } else {
                        log.error("Error fetching resources since {}", sinceTimestamp, e);
                    }
                });
    }

    @Data
    private static class LastUpdated {
        private Long lastUpdated;
    }

    public Mono<List<SakResource>> findCasesWithFilter(String caseFilter) {
        return Mono.defer(() -> {
                    Timer.Sample sample = Timer.start(meterRegistry);

                    return fintWebClient
                            .post()
                            .uri("/arkiv/noark/sak/$query")
                            .headers(headers -> headers.setContentType(MediaType.TEXT_PLAIN))
                            .httpRequest(clientHttpRequest -> {
                                HttpClientRequest reactorRequest = clientHttpRequest.getNativeRequest();
                                reactorRequest.responseTimeout(Duration.ofMillis(
                                        properties.getFindCasesWithFilterTimeoutMillis()
                                ));
                            })
                            .bodyValue(caseFilter)
                            .retrieve()
                            .bodyToMono(SakResources.class)
                            .map(SakResources::getContent)
                            .onErrorReturn(WebClientResponseException.NotFound.class, List.of())
                            .doOnError(webUtilErrorHandler::logAndSendError)
                            .doFinally(signal -> sample.stop(findCasesTimer));
                })
                .retryWhen(
                        Retry.backoff(
                                properties.getFindCasesWithFilterMaxAttempts() - 1,
                                Duration.ofMillis(properties.getFindCasesWithFilterBackoffRetryMinDelayMillis())
                        ).maxBackoff(
                                Duration.ofMillis(properties.getFindCasesWithFilterBackoffRetryMaxDelayMillis())
                        ).doBeforeRetry(
                                retrySignal -> log.warn("Encountered error when finding cases with filter -- performing retry {}", retrySignal.totalRetries() + 1, retrySignal.failure())
                        )
                );
    }

    public <T> Mono<T> getResource(String endpoint, Class<T> clazz) {
        return fintWebClient.get()
                .uri(endpoint)
                .httpRequest(clientHttpRequest -> {
                    HttpClientRequest reactorRequest = clientHttpRequest.getNativeRequest();
                    reactorRequest.responseTimeout(Duration.ofMillis(
                            properties.getGetResourceTimeoutMillis()
                    ));
                })
                .retrieve()
                .bodyToMono(clazz);
    }

    public Mono<SakResource> getCase(URI uri) {
        return getResource(uri.getPath(), SakResource.class);
    }

}
