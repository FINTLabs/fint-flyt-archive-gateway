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
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClientRequest
import reactor.util.retry.Retry
import java.net.URI

@Component
class FintArchiveResourceClient(
    private val properties: FintArchiveResourceClientProperties,
    @param:Qualifier("fintWebClient")
    private val fintWebClient: WebClient,
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

    fun getLastUpdated(urlResourcePath: String): Mono<Long> {
        val sample = Timer.start(meterRegistry)
        return fintWebClient
            .get()
            .uri("$urlResourcePath/last-updated")
            .httpRequest { clientHttpRequest ->
                val reactorRequest = clientHttpRequest.getNativeRequest<HttpClientRequest>()
                reactorRequest.responseTimeout(requireNotNull(properties.getResourcesLastUpdatedTimeout))
            }.retrieve()
            .bodyToMono(LastUpdated::class.java)
            .map { it.lastUpdated }
            .doFinally { sample.stop(lastUpdatedTimer) }
    }

    fun <T> getResourcesSince(
        urlResourcePath: String,
        resourceClass: Class<T>,
        sinceTimestamp: Long,
    ): Flux<T> =
        fintWebClient
            .get()
            .uri { uriBuilder ->
                uriBuilder
                    .path(urlResourcePath)
                    .queryParam("sinceTimeStamp", sinceTimestamp)
                    .build()
            }.retrieve()
            .bodyToMono(object : ParameterizedTypeReference<ResourceCollection>() {})
            .flatMapIterable(AbstractCollectionResources<Any>::getContent)
            .map { resource -> objectMapper.convertValue(resource, resourceClass) }
            .doOnError { error ->
                if (error is WebClientResponseException) {
                    log.error("{} body={}", error, error.responseBodyAsString)
                } else {
                    log.error("Error fetching resources since {}", sinceTimestamp, error)
                }
            }

    fun findCasesWithFilter(caseFilter: String): Mono<List<SakResource>> =
        Mono
            .defer {
                val sample = Timer.start(meterRegistry)
                fintWebClient
                    .post()
                    .uri("/arkiv/noark/sak/\$query")
                    .headers { it.contentType = MediaType.TEXT_PLAIN }
                    .httpRequest { clientHttpRequest ->
                        val reactorRequest = clientHttpRequest.getNativeRequest<HttpClientRequest>()
                        reactorRequest.responseTimeout(requireNotNull(properties.findCasesWithFilterTimeout))
                    }.bodyValue(caseFilter)
                    .retrieve()
                    .bodyToMono(SakResources::class.java)
                    .map { sakResources ->
                        val content: List<SakResource> = sakResources.content?.toList() ?: emptyList()
                        content
                    }.onErrorReturn(WebClientResponseException.NotFound::class.java, emptyList())
                    .doOnError(webUtilErrorHandler::logAndSendError)
                    .doFinally { sample.stop(findCasesTimer) }
            }.retryWhen(
                Retry
                    .backoff(
                        requireNotNull(properties.findCasesWithFilterMaxAttempts) - 1,
                        requireNotNull(properties.findCasesWithFilterBackoffRetryMinDelay),
                    ).maxBackoff(requireNotNull(properties.findCasesWithFilterBackoffRetryMaxDelay))
                    .doBeforeRetry { retrySignal ->
                        log.warn(
                            "Encountered error when finding cases with filter -- performing retry {}",
                            retrySignal.totalRetries() + 1,
                            retrySignal.failure(),
                        )
                    },
            )

    fun <T> getResource(
        endpoint: String,
        clazz: Class<T>,
    ): Mono<T> =
        fintWebClient
            .get()
            .uri(endpoint)
            .httpRequest { clientHttpRequest ->
                val reactorRequest = clientHttpRequest.getNativeRequest<HttpClientRequest>()
                reactorRequest.responseTimeout(requireNotNull(properties.getResourceTimeout))
            }.retrieve()
            .bodyToMono(clazz)

    fun getCase(uri: URI): Mono<SakResource> = getResource(uri.path, SakResource::class.java)

    data class LastUpdated(
        val lastUpdated: Long,
    )

    companion object {
        private val log = LoggerFactory.getLogger(FintArchiveResourceClient::class.java)
    }
}
