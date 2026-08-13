package no.novari.flyt.archive.gateway.dispatch.web

import io.github.oshai.kotlinlogging.KotlinLogging
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import no.novari.fint.model.resource.Link
import no.novari.fint.model.resource.arkiv.noark.JournalpostResource
import no.novari.fint.model.resource.arkiv.noark.SakResource
import no.novari.flyt.archive.gateway.WebUtilErrorHandler
import no.novari.flyt.archive.gateway.dispatch.model.File
import no.novari.flyt.archive.gateway.dispatch.model.JournalpostWrapper
import no.novari.flyt.archive.gateway.resource.web.FintArchiveResourceClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.InvalidMediaTypeException
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import java.net.URI
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.NoSuchElementException

@Service
class FintArchiveDispatchClient(
    private val properties: FintArchiveDispatchClientConfigurationProperties,
    @param:Qualifier("fintRestClient")
    private val fintRestClient: RestClient,
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

    fun postFile(file: File): Link {
        val sample = Timer.start(meterRegistry)
        try {
            log.info { "Posting file" }
            val response =
                fintRestClient
                    .post()
                    .uri("/arkiv/noark/dokumentfil")
                    .contentType(getMediaType(file.type ?: MediaType.APPLICATION_OCTET_STREAM_VALUE))
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDispositionHeader(file.name.orEmpty()))
                    .body(requireNotNull(file.contents) { "File contents are required for dispatch" })
                    .retrieve()
                    .toBodilessEntity()
            val createdLocation = pollAfterAcceptedResponse(response)
            val link = Link.with(createdLocation.toString())
            log.atInfo {
                message = "Successfully posted file name={} uri={}"
                arguments = arrayOf(file.name, createdLocation)
            }
            return link
        } catch (error: Throwable) {
            webUtilErrorHandler.logAndSendError(error)
            throw error
        } finally {
            sample.stop(postFileTimer)
        }
    }

    fun postCase(sakResource: SakResource): SakResource {
        val sample = Timer.start(meterRegistry)
        try {
            log.info { "Posting case" }
            val response =
                fintRestClient
                    .post()
                    .uri("/arkiv/noark/sak")
                    .body(sakResource)
                    .retrieve()
                    .toBodilessEntity()
            return pollForCaseResult(response)
        } catch (error: Throwable) {
            webUtilErrorHandler.logAndSendError(error)
            throw error
        } finally {
            sample.stop(postCaseTimer)
        }
    }

    fun postRecord(
        caseId: String,
        journalpostResource: JournalpostResource,
    ): JournalpostResource {
        val sample = Timer.start(meterRegistry)
        try {
            log.atInfo {
                message = "Posting record caseId={}"
                arguments = arrayOf(caseId)
            }
            val response =
                fintRestClient
                    .put()
                    .uri("/arkiv/noark/sak/mappeid/$caseId")
                    .body(JournalpostWrapper(journalpostResource))
                    .retrieve()
                    .toBodilessEntity()
            val sak = pollForCaseResult(response)
            val journalposts: List<JournalpostResource> = sak.journalpost?.toList() ?: emptyList()
            return journalposts
                .mapNotNull { journalpost ->
                    journalpost.journalPostnummer?.let { journalpostId -> journalpostId to journalpost }
                }.maxByOrNull { (journalpostId, _) -> journalpostId }
                ?.second
                ?: throw NoSuchElementException("Journalpost is missing journalpostNummer for case $caseId")
        } catch (error: Throwable) {
            webUtilErrorHandler.logAndSendError(error)
            throw error
        } finally {
            sample.stop(postRecordTimer)
        }
    }

    private fun getMediaType(mediaType: String): MediaType =
        try {
            MediaType.parseMediaType(mediaType)
        } catch (_: InvalidMediaTypeException) {
            MediaType.APPLICATION_OCTET_STREAM
        }

    private fun pollForCaseResult(initialResponse: ResponseEntity<Void>): SakResource {
        val createdLocation = pollAfterAcceptedResponse(initialResponse)
        return fintArchiveResourceClient.getCase(createdLocation)
    }

    private fun pollAfterAcceptedResponse(initialResponse: ResponseEntity<Void>): URI {
        val statusLocation = getStatusLocation(initialResponse)
        Thread.sleep(200)
        return pollForCreatedLocation(statusLocation)
    }

    private fun getStatusLocation(entity: ResponseEntity<Void>): URI {
        log.atDebug {
            message = "Received status response status={} location={}"
            arguments = arrayOf(entity.statusCode, entity.headers.location)
        }
        val location = entity.headers.location
        if (entity.statusCode == HttpStatus.ACCEPTED && location != null) {
            return location
        }
        throw RuntimeException(
            "Expected 202 Accepted response with redirect header, got status=" +
                "${entity.statusCode} location=${entity.headers.location}",
        )
    }

    internal fun pollForCreatedLocation(statusUri: URI): URI {
        val totalTimeout = requireNotNull(properties.createdLocationPollTotalTimeout)
        val minDelay = requireNotNull(properties.createdLocationPollBackoffMinDelay)
        val maxDelay = requireNotNull(properties.createdLocationPollBackoffMaxDelay)

        log.atInfo {
            message = "Polling for created location statusUri={} totalTimeout={} backoffMinDelay={} backoffMaxDelay={}"
            arguments = arrayOf(statusUri, totalTimeout, minDelay, maxDelay)
        }

        val deadline = Instant.now().plus(totalTimeout)
        var delay = minDelay
        var lastStatus: String?

        while (true) {
            val sample = Timer.start(meterRegistry)
            try {
                val entity =
                    fintRestClient
                        .get()
                        .uri(statusUri)
                        .retrieve()
                        .toBodilessEntity()
                lastStatus = entity.statusCode.toString()
                log.atDebug {
                    message = "Poll response statusUri={} status={} location={}"
                    arguments = arrayOf(statusUri, entity.statusCode, entity.headers.location)
                }
                val location = entity.headers.location
                if (entity.statusCode == HttpStatus.CREATED && location != null) {
                    return location
                }
            } finally {
                sample.stop(pollForCreationTimer)
            }

            if (Instant.now().isAfter(deadline)) {
                break
            }
            Thread.sleep(delay.toMillis())
            delay = minOf(delay.multipliedBy(2), maxDelay)
        }

        log.atWarn {
            message = "Timed out polling created location statusUri={} totalTimeout={} lastStatus={}"
            arguments = arrayOf(statusUri, totalTimeout, lastStatus)
        }
        throw CreatedLocationPollTimeoutException(statusUri, totalTimeout, lastStatus)
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}

internal fun contentDispositionHeader(fileName: String): String =
    ContentDisposition
        .attachment()
        .filename(fileName, StandardCharsets.UTF_8)
        .build()
        .toString()
