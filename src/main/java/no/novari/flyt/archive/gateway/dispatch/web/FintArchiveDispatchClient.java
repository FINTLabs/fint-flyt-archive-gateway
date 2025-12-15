package no.novari.flyt.archive.gateway.dispatch.web;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.JournalpostResource;
import no.fint.model.resource.arkiv.noark.SakResource;
import no.novari.flyt.archive.gateway.WebUtilErrorHandler;
import no.novari.flyt.archive.gateway.dispatch.model.File;
import no.novari.flyt.archive.gateway.dispatch.model.JournalpostWrapper;
import no.novari.flyt.archive.gateway.resource.web.FintArchiveResourceClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClientRequest;
import reactor.retry.Repeat;

import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
@Service
public class FintArchiveDispatchClient {

    private final FintArchiveDispatchClientConfigurationProperties properties;
    private final WebClient fintWebClient;
    private final FintArchiveResourceClient fintArchiveResourceClient;

    private final Timer postFileTimer;
    private final Timer postCaseTimer;
    private final Timer postRecordTimer;
    private final Timer pollForCreationTimer;
    private final MeterRegistry meterRegistry;

    private final WebUtilErrorHandler webUtilErrorHandler;

    public FintArchiveDispatchClient(
            FintArchiveDispatchClientConfigurationProperties fintArchiveDispatchClientConfigurationProperties,
            @Qualifier("fintWebClient") WebClient fintWebClient,
            FintArchiveResourceClient fintArchiveResourceClient,
            MeterRegistry meterRegistry,
            WebUtilErrorHandler webUtilErrorHandler
    ) {
        this.properties = fintArchiveDispatchClientConfigurationProperties;
        this.fintWebClient = fintWebClient;
        this.fintArchiveResourceClient = fintArchiveResourceClient;
        this.meterRegistry = meterRegistry;
        this.postFileTimer = Timer.builder("novari.flyt.gateway.archive.dispatch.postFile").description("Timing postFile").register(meterRegistry);
        this.postCaseTimer = Timer.builder("novari.flyt.gateway.archive.dispatch.postCase").description("Timing postCase").register(meterRegistry);
        this.postRecordTimer = Timer.builder("novari.flyt.gateway.archive.dispatch.postRecord").description("Timing postRecord").register(meterRegistry);
        this.pollForCreationTimer = Timer.builder("novari.flyt.gateway.archive.dispatch.pollForCreatedLocation").description("Timing poll for created location").register(meterRegistry);
        this.webUtilErrorHandler = webUtilErrorHandler;
    }

    public Mono<Link> postFile(File file) {
        return Mono.defer(() -> {
            Timer.Sample sample = Timer.start(meterRegistry);
            log.info("Posting file");
            return pollForCreatedLocation(
                    fintWebClient
                            .post()
                            .uri("/arkiv/noark/dokumentfil")
                            .httpRequest(clientHttpRequest -> {
                                HttpClientRequest reactorRequest = clientHttpRequest.getNativeRequest();
                                reactorRequest.responseTimeout(properties.getPostFileTimeout());
                            })
                            .contentType(getMediaType(file.getType()))
                            .bodyValue(file.getContents())
                            .header("Content-Disposition", "attachment; filename=" + file.getName())
                            .retrieve()
            )
                    .map(URI::toString)
                    .map(Link::with)
                    .doOnNext(uri -> log.info("Successfully posted file name={} uri={}", file.getName(), uri))
                    .doOnError(webUtilErrorHandler::logAndSendError)
                    .doFinally(sig -> sample.stop(postFileTimer));
        });
    }

    private MediaType getMediaType(String mediaType) {
        try {
            return MediaType.parseMediaType(mediaType);
        } catch (InvalidMediaTypeException e) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    public Mono<SakResource> postCase(SakResource sakResource) {
        return Mono.defer(() -> {
            Timer.Sample sample = Timer.start(meterRegistry);
            return pollForCaseResult(
                    fintWebClient
                            .post()
                            .uri("/arkiv/noark/sak")
                            .httpRequest(clientHttpRequest -> {
                                HttpClientRequest reactorRequest = clientHttpRequest.getNativeRequest();
                                reactorRequest.responseTimeout(properties.getPostCaseTimeout());
                            })
                            .bodyValue(sakResource)
                            .retrieve()
            )
                    .doOnError(webUtilErrorHandler::logAndSendError)
                    .doFinally(sig -> sample.stop(postCaseTimer));
        });
    }

    public Mono<JournalpostResource> postRecord(String caseId, JournalpostResource journalpostResource) {
        return Mono.defer(() -> {
            Timer.Sample sample = Timer.start(meterRegistry);
            return pollForCaseResult(
                    fintWebClient
                            .put()
                            .uri("/arkiv/noark/sak/mappeid/" + caseId)
                            .httpRequest(clientHttpRequest -> {
                                HttpClientRequest reactorRequest = clientHttpRequest.getNativeRequest();
                                reactorRequest.responseTimeout(properties.getPostRecordTimeout());
                            })
                            .bodyValue(new JournalpostWrapper(journalpostResource))
                            .retrieve()
            )
                    .map(sak -> Optional.ofNullable(sak.getJournalpost())
                            .orElseGet(Collections::emptyList)
                            .stream()
                            .filter(jp -> jp.getJournalPostnummer() != null)
                            .max(Comparator.comparing(JournalpostResource::getJournalPostnummer))
                            .orElseThrow(() -> new NoSuchElementException(
                                    "Journalpost is missing journalpostNummer for case " + caseId))
                    )
                    .doFinally(sig -> sample.stop(postRecordTimer));
        });
    }

    private Mono<SakResource> pollForCaseResult(WebClient.ResponseSpec responseSpec) {
        return pollForCreatedLocation(responseSpec)
                .flatMap(fintArchiveResourceClient::getCase)
                .doOnError(webUtilErrorHandler::logAndSendError);
    }

    private Mono<URI> pollForCreatedLocation(WebClient.ResponseSpec responseSpec) {
        return getStatusLocation(responseSpec)
                .delayElement(Duration.ofMillis(200))
                .flatMap(this::pollForCreatedLocation);
    }

    private Mono<URI> getStatusLocation(WebClient.ResponseSpec responseSpec) {
        return responseSpec
                .toBodilessEntity()
                .handle((entity, sink) -> {
                            if (HttpStatus.ACCEPTED.equals(entity.getStatusCode())
                                    && entity.getHeaders().getLocation() != null) {
                                sink.next(entity.getHeaders().getLocation());
                            } else {
                                sink.error(new RuntimeException("Expected 202 Accepted response with redirect header"));
                            }
                        }
                );
    }

    protected Mono<URI> pollForCreatedLocation(URI statusUri) {
        return Mono.defer(() -> {
                            Timer.Sample sample = Timer.start(meterRegistry);
                            return fintWebClient
                                    .get()
                                    .uri(statusUri)
                                    .httpRequest(clientHttpRequest -> {
                                        HttpClientRequest reactorRequest = clientHttpRequest.getNativeRequest();
                                        reactorRequest.responseTimeout(properties.getGetStatusTimeout());
                                    })
                                    .retrieve()
                                    .toBodilessEntity()
                                    .doOnError(webUtilErrorHandler::logAndSendError)
                                    .doFinally(entity -> sample.stop(pollForCreationTimer));
                        }
                )
                .filter(entity -> HttpStatus.CREATED.equals(entity.getStatusCode()) && entity.getHeaders().getLocation() != null)
                .mapNotNull(entity -> entity.getHeaders().getLocation())
                .repeatWhenEmpty(
                        Repeat
                                .times(Long.MAX_VALUE)
                                .exponentialBackoff(
                                        properties.getCreatedLocationPollBackoffMinDelay(),
                                        properties.getCreatedLocationPollBackoffMaxDelay()
                                ).timeout(properties.getCreatedLocationPollTotalTimeout())
                )
                .switchIfEmpty(Mono.error(new CreatedLocationPollTimeoutException()));
    }
}
