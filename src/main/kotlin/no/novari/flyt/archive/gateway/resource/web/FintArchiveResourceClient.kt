package no.novari.flyt.archive.gateway.resource.web

import com.fasterxml.jackson.databind.ObjectMapper
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import no.novari.fint.model.resource.AbstractCollectionResources
import no.novari.fint.model.resource.arkiv.noark.SakResource
import no.novari.fint.model.resource.arkiv.noark.SakResources
import no.novari.flyt.archive.gateway.WebUtilErrorHandler
import no.novari.flyt.archive.gateway.resource.model.ResourceCollection
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.body
import java.net.URI

@Component
class FintArchiveResourceClient(
    private val properties: FintArchiveResourceClientProperties,
    @param:Qualifier("fintRestClient")
    private val fintRestClient: RestClient,
    private val meterRegistry: MeterRegistry,
    private val webUtilErrorHandler: WebUtilErrorHandler,
) {
    private val objectMapper = ObjectMapper()
    private val lastUpdatedTimer: Timer =
        Timer
            .builder("novari.flyt.gateway.archive.resource.getResourcesLastUpdated")
            .description("Time to fetch and map last-updated resources")
            .register(meterRegistry)
    private val findCasesTimer: Timer =
        Timer
            .builder("novari.flyt.gateway.archive.resource.findCasesWithFilter")
            .description("Time to fetch cases with filter")
            .register(meterRegistry)

    fun getLastUpdated(urlResourcePath: String): Long? {
        val sample = Timer.start(meterRegistry)
        try {
            return fintRestClient
                .get()
                .uri("$urlResourcePath/last-updated")
                .retrieve()
                .body<LastUpdated>()
                ?.lastUpdated
        } finally {
            sample.stop(lastUpdatedTimer)
        }
    }

    fun <T> getResourcesSince(
        urlResourcePath: String,
        resourceClass: Class<T>,
        sinceTimestamp: Long,
    ): List<T> =
        try {
            val collection =
                fintRestClient
                    .get()
                    .uri { uriBuilder ->
                        uriBuilder
                            .path(urlResourcePath)
                            .queryParam("sinceTimeStamp", sinceTimestamp)
                            .build()
                    }.retrieve()
                    .body(object : ParameterizedTypeReference<ResourceCollection>() {})

            collection
                ?.let(AbstractCollectionResources<Any>::getContent)
                .orEmpty()
                .map { resource -> objectMapper.convertValue(resource, resourceClass) }
        } catch (error: Exception) {
            if (error is RestClientResponseException) {
                log.error("{} body={}", error, error.responseBodyAsString)
            } else {
                log.error("Error fetching resources since {}", sinceTimestamp, error)
            }
            throw error
        }

    fun findCasesWithFilter(caseFilter: String): List<SakResource> {
        val maxAttempts = requireNotNull(properties.findCasesWithFilterMaxAttempts)
        val minDelay = requireNotNull(properties.findCasesWithFilterBackoffRetryMinDelay)
        val maxDelay = requireNotNull(properties.findCasesWithFilterBackoffRetryMaxDelay)

        var attempt = 0L
        var delay = minDelay
        while (true) {
            val sample = Timer.start(meterRegistry)
            try {
                val sakResources =
                    fintRestClient
                        .post()
                        .uri("/arkiv/noark/sak/\$query")
                        .contentType(MediaType.TEXT_PLAIN)
                        .body(caseFilter)
                        .retrieve()
                        .body<SakResources>()
                return sakResources?.content?.toList().orEmpty()
            } catch (_: HttpClientErrorException.NotFound) {
                return emptyList()
            } catch (error: Throwable) {
                webUtilErrorHandler.logAndSendError(error)
                attempt++
                if (attempt >= maxAttempts) {
                    throw error
                }
                log.warn(
                    "Encountered error when finding cases with filter -- performing retry {}",
                    attempt,
                    error,
                )
                Thread.sleep(delay.toMillis())
                delay = minOf(delay.multipliedBy(2), maxDelay)
            } finally {
                sample.stop(findCasesTimer)
            }
        }
    }

    fun <T> getResource(
        endpoint: String,
        clazz: Class<T>,
    ): T =
        requireNotNull(
            fintRestClient
                .get()
                .uri(endpoint)
                .retrieve()
                .body(clazz),
        ) { "Empty response body from $endpoint" }

    fun getCase(uri: URI): SakResource = getResource(uri.path, SakResource::class.java)

    data class LastUpdated(
        val lastUpdated: Long,
    )

    companion object {
        private val log = LoggerFactory.getLogger(FintArchiveResourceClient::class.java)
    }
}
