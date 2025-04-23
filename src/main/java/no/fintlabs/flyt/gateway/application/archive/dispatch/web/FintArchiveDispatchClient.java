package no.fintlabs.flyt.gateway.application.archive.dispatch.web;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.JournalpostResource;
import no.fint.model.resource.arkiv.noark.SakResource;
import no.fintlabs.flyt.gateway.application.archive.WebUtilErrorHandler;
import no.fintlabs.flyt.gateway.application.archive.dispatch.model.File;
import no.fintlabs.flyt.gateway.application.archive.dispatch.model.JournalpostWrapper;
import no.fintlabs.flyt.gateway.application.archive.resource.web.FintArchiveResourceClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.retry.Repeat;

import java.net.URI;
import java.time.Duration;
import java.util.Comparator;

@Slf4j
@Service
public class FintArchiveDispatchClient {

    private final Integer createdLocationPollTimes;
    private final Long createdLocationPollMinDelayMillis;
    private final Long createdLocationPollMaxDelayMillis;
    private final WebClient fintWebClient;
    private final FintArchiveResourceClient fintArchiveResourceClient;

    private final Timer postFileTimer;
    private final Timer postCaseTimer;
    private final Timer postRecordTimer;
    private final Timer pollForCreationTimer;
    private final MeterRegistry meterRegistry;

    private final WebUtilErrorHandler webUtilErrorHandler;

    public FintArchiveDispatchClient(
            @Value("${fint.flyt.gateway.application.archive.client.fint-archive.created-location-polling.attempts}") Integer createdLocationPollAttempts,
            @Value("${fint.flyt.gateway.application.archive.client.fint-archive.created-location-polling.min-delay-millis}") Long createdLocationPollMinDelayMillis,
            @Value("${fint.flyt.gateway.application.archive.client.fint-archive.created-location-polling.max-delay-millis}") Long createdLocationPollMaxDelayMillis,
            @Qualifier("fintWebClient") WebClient fintWebClient,
            FintArchiveResourceClient fintArchiveResourceClient,
            MeterRegistry meterRegistry,
            WebUtilErrorHandler webUtilErrorHandler
    ) {
        this.createdLocationPollTimes = createdLocationPollAttempts;
        this.createdLocationPollMinDelayMillis = createdLocationPollMinDelayMillis;
        this.createdLocationPollMaxDelayMillis = createdLocationPollMaxDelayMillis;
        this.fintWebClient = fintWebClient;
        this.fintArchiveResourceClient = fintArchiveResourceClient;
        this.meterRegistry = meterRegistry;
        this.postFileTimer = Timer.builder("fint.flyt.gateway.archive.dispatch.postFile").description("Timing postFile").register(meterRegistry);
        this.postCaseTimer = Timer.builder("fint.flyt.gateway.archive.dispatch.postCase").description("Timing postCase").register(meterRegistry);
        this.postRecordTimer = Timer.builder("fint.flyt.gateway.archive.dispatch.postRecord").description("Timing postRecord").register(meterRegistry);
        this.pollForCreationTimer = Timer.builder("fint.flyt.gateway.archive.dispatch.pollForCreatedLocation").description("Timing poll for created location").register(meterRegistry);
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
                            .bodyValue(new JournalpostWrapper(journalpostResource))
                            .retrieve()
            )
                    .map(sak -> sak.getJournalpost()
                            .stream()
                            .max(Comparator.comparing(JournalpostResource::getJournalPostnummer))
                            .orElseThrow()
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
                                .times(createdLocationPollTimes - 1)
                                .exponentialBackoff(
                                        Duration.ofMillis(createdLocationPollMinDelayMillis),
                                        Duration.ofMillis(createdLocationPollMaxDelayMillis)
                                )
                )
                .switchIfEmpty(Mono.error(new RuntimeException("Reached max number of retries for polling created location from destination")));
    }
}
