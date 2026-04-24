package no.novari.flyt.archive.gateway.dispatch.web

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import no.novari.fint.model.resource.Link
import no.novari.fint.model.resource.arkiv.noark.JournalpostResource
import no.novari.fint.model.resource.arkiv.noark.SakResource
import no.novari.flyt.archive.gateway.WebUtilErrorHandler
import no.novari.flyt.archive.gateway.dispatch.model.File
import no.novari.flyt.archive.gateway.dispatch.model.JournalpostWrapper
import no.novari.flyt.archive.gateway.resource.web.FintArchiveResourceClient
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.http.InvalidMediaTypeException
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClientRequest
import reactor.retry.Repeat
import java.net.URI
import java.time.Duration
import java.util.NoSuchElementException
import java.util.concurrent.atomic.AtomicReference

@Service
class FintArchiveDispatchClient(
    private val properties: FintArchiveDispatchClientConfigurationProperties,
    @param:Qualifier("fintWebClient")
    private val fintWebClient: WebClient,
    private val fintArchiveResourceClient: FintArchiveResourceClient,
    private val meterRegistry: MeterRegistry,
    private val webUtilErrorHandler: WebUtilErrorHandler,
) {
    private val postFileTimer: Timer =
        Timer
            .builder("novari.flyt.gateway.archive.dispatch.postFile")
            .description("Timing postFile")
            .register(meterRegistry)
    private val postCaseTimer: Timer =
        Timer
            .builder("novari.flyt.gateway.archive.dispatch.postCase")
            .description("Timing postCase")
            .register(meterRegistry)
    private val postRecordTimer: Timer =
        Timer
            .builder("novari.flyt.gateway.archive.dispatch.postRecord")
            .description("Timing postRecord")
            .register(meterRegistry)
    private val pollForCreationTimer: Timer =
        Timer
            .builder("novari.flyt.gateway.archive.dispatch.pollForCreatedLocation")
            .description("Timing poll for created location")
            .register(meterRegistry)

    fun postFile(file: File): Mono<Link> =
        Mono.defer {
            val sample = Timer.start(meterRegistry)
            log.info("Posting file")
            pollForCreatedLocation(
                fintWebClient
                    .post()
                    .uri("/arkiv/noark/dokumentfil")
                    .httpRequest { clientHttpRequest ->
                        val reactorRequest = clientHttpRequest.getNativeRequest<HttpClientRequest>()
                        reactorRequest.responseTimeout(requireNotNull(properties.postFileTimeout))
                    }.contentType(getMediaType(file.type ?: MediaType.APPLICATION_OCTET_STREAM_VALUE))
                    .bodyValue(requireNotNull(file.contents) { "File contents are required for dispatch" })
                    .header("Content-Disposition", "attachment; filename=${file.name.orEmpty()}")
                    .retrieve(),
            ).map(URI::toString)
                .map(Link::with)
                .doOnNext { uri -> log.info("Successfully posted file name={} uri={}", file.name, uri) }
                .doOnError(webUtilErrorHandler::logAndSendError)
                .doFinally { sample.stop(postFileTimer) }
        }

    fun postCase(sakResource: SakResource): Mono<SakResource> =
        Mono.defer {
            val sample = Timer.start(meterRegistry)
            log.info("Posting case")
            pollForCaseResult(
                fintWebClient
                    .post()
                    .uri("/arkiv/noark/sak")
                    .httpRequest { clientHttpRequest ->
                        val reactorRequest = clientHttpRequest.getNativeRequest<HttpClientRequest>()
                        reactorRequest.responseTimeout(requireNotNull(properties.postCaseTimeout))
                    }.bodyValue(sakResource)
                    .retrieve(),
            ).doOnError(webUtilErrorHandler::logAndSendError)
                .doFinally { sample.stop(postCaseTimer) }
        }

    fun postRecord(
        caseId: String,
        journalpostResource: JournalpostResource,
    ): Mono<JournalpostResource> =
        Mono.defer {
            val sample = Timer.start(meterRegistry)
            log.info("Posting record caseId={}", caseId)
            pollForCaseResult(
                fintWebClient
                    .put()
                    .uri("/arkiv/noark/sak/mappeid/$caseId")
                    .httpRequest { clientHttpRequest ->
                        val reactorRequest = clientHttpRequest.getNativeRequest<HttpClientRequest>()
                        reactorRequest.responseTimeout(requireNotNull(properties.postRecordTimeout))
                    }.bodyValue(JournalpostWrapper(journalpostResource))
                    .retrieve(),
            ).map { sak ->
                val journalposts: List<JournalpostResource> = sak.journalpost?.toList() ?: emptyList()
                journalposts
                    .mapNotNull { journalpost ->
                        journalpost.journalPostnummer?.let { journalpostId -> journalpostId to journalpost }
                    }.maxByOrNull { (journalpostId, _) -> journalpostId }
                    ?.second
                    ?: throw NoSuchElementException("Journalpost is missing journalpostNummer for case $caseId")
            }.doFinally { sample.stop(postRecordTimer) }
        }

    private fun getMediaType(mediaType: String): MediaType =
        try {
            MediaType.parseMediaType(mediaType)
        } catch (_: InvalidMediaTypeException) {
            MediaType.APPLICATION_OCTET_STREAM
        }

    private fun pollForCaseResult(responseSpec: WebClient.ResponseSpec): Mono<SakResource> =
        pollForCreatedLocation(responseSpec)
            .flatMap(fintArchiveResourceClient::getCase)
            .doOnError(webUtilErrorHandler::logAndSendError)

    private fun pollForCreatedLocation(responseSpec: WebClient.ResponseSpec): Mono<URI> =
        getStatusLocation(responseSpec)
            .delayElement(Duration.ofMillis(200))
            .flatMap(this::pollForCreatedLocation)

    private fun getStatusLocation(responseSpec: WebClient.ResponseSpec): Mono<URI> =
        responseSpec
            .toBodilessEntity()
            .doOnNext { entity ->
                log.debug(
                    "Received status response status={} location={}",
                    entity.statusCode,
                    entity.headers.location,
                )
            }.handle<URI> { entity, sink ->
                val location = entity.headers.location
                if (entity.statusCode == HttpStatus.ACCEPTED && location != null) {
                    sink.next(location)
                } else {
                    sink.error(
                        RuntimeException(
                            "Expected 202 Accepted response with redirect header, got status=" +
                                "${entity.statusCode} location=${entity.headers.location}",
                        ),
                    )
                }
            }

    protected fun pollForCreatedLocation(statusUri: URI): Mono<URI> {
        val lastStatus = AtomicReference<String>()
        return Mono
            .defer {
                val sample = Timer.start(meterRegistry)
                log.info(
                    "Polling for created location statusUri={} totalTimeout={} backoffMinDelay={} backoffMaxDelay={}",
                    statusUri,
                    properties.createdLocationPollTotalTimeout,
                    properties.createdLocationPollBackoffMinDelay,
                    properties.createdLocationPollBackoffMaxDelay,
                )
                fintWebClient
                    .get()
                    .uri(statusUri)
                    .httpRequest { clientHttpRequest ->
                        val reactorRequest = clientHttpRequest.getNativeRequest<HttpClientRequest>()
                        reactorRequest.responseTimeout(requireNotNull(properties.getStatusTimeout))
                    }.retrieve()
                    .toBodilessEntity()
                    .doOnNext { entity ->
                        lastStatus.set(entity.statusCode.toString())
                        log.debug(
                            "Poll response statusUri={} status={} location={}",
                            statusUri,
                            entity.statusCode,
                            entity.headers.location,
                        )
                    }.doOnError(webUtilErrorHandler::logAndSendError)
                    .doFinally { sample.stop(pollForCreationTimer) }
            }.handle<URI> { entity, sink ->
                val location = entity.headers.location
                if (entity.statusCode == HttpStatus.CREATED && location != null) {
                    sink.next(location)
                } else {
                    sink.complete()
                }
            }.repeatWhenEmpty(
                Repeat
                    .times<URI>(Long.MAX_VALUE)
                    .exponentialBackoff(
                        requireNotNull(properties.createdLocationPollBackoffMinDelay),
                        requireNotNull(properties.createdLocationPollBackoffMaxDelay),
                    ).timeout(requireNotNull(properties.createdLocationPollTotalTimeout)),
            ).switchIfEmpty(
                Mono.defer<URI> {
                    Mono.error(
                        CreatedLocationPollTimeoutException(
                            statusUri,
                            requireNotNull(properties.createdLocationPollTotalTimeout),
                            lastStatus.get(),
                        ),
                    )
                },
            ).doOnError(CreatedLocationPollTimeoutException::class.java, webUtilErrorHandler::logAndSendError)
            .doOnError(CreatedLocationPollTimeoutException::class.java) {
                log.warn(
                    "Timed out polling created location statusUri={} totalTimeout={} lastStatus={}",
                    statusUri,
                    properties.createdLocationPollTotalTimeout,
                    lastStatus.get(),
                )
            }
    }

    companion object {
        private val log = LoggerFactory.getLogger(FintArchiveDispatchClient::class.java)
    }
}
