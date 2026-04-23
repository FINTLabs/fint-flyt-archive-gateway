package no.novari.flyt.archive.gateway.dispatch.web

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import no.novari.flyt.archive.gateway.WebUtilErrorHandler
import no.novari.flyt.archive.gateway.resource.web.FintArchiveResourceClient
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.atLeast
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.ResponseEntity
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientException
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.net.URI
import java.time.Duration

class FintArchiveDispatchClientTest {
    private lateinit var fintWebClient: WebClient
    private lateinit var fintArchiveResourceClient: FintArchiveResourceClient
    private lateinit var fintArchiveDispatchClient: TestableFintArchiveDispatchClient
    private lateinit var meterRegistry: MeterRegistry
    private lateinit var webUtilErrorHandler: WebUtilErrorHandler

    @BeforeEach
    fun setup() {
        fintWebClient = mock()
        fintArchiveResourceClient = mock()
        meterRegistry = SimpleMeterRegistry()
        webUtilErrorHandler = mock()
        val properties =
            FintArchiveDispatchClientConfigurationProperties().apply {
                postFileTimeout = Duration.ofMillis(130000L)
                postRecordTimeout = Duration.ofMillis(130000L)
                postCaseTimeout = Duration.ofMillis(130000L)
                getStatusTimeout = Duration.ofMillis(130000L)
                createdLocationPollBackoffMinDelay = Duration.ofMillis(100L)
                createdLocationPollBackoffMaxDelay = Duration.ofMillis(250L)
                createdLocationPollTotalTimeout = Duration.ofMillis(1000L)
            }
        fintArchiveDispatchClient =
            TestableFintArchiveDispatchClient(
                properties,
                fintWebClient,
                fintArchiveResourceClient,
                meterRegistry,
                webUtilErrorHandler,
            )
    }

    @Test
    fun givenCreatedLocationOnFirstPollWhenPollForCreatedLocationShouldReturnWithoutRepeats() {
        val requestHeadersUriSpec = mock<WebClient.RequestHeadersUriSpec<*>>()
        val requestBodySpec = mock<WebClient.RequestBodySpec>()
        val responseSpec = mock<WebClient.ResponseSpec>()
        whenever(fintWebClient.get()).thenReturn(requestHeadersUriSpec)
        whenever(requestHeadersUriSpec.uri(URI.create("testStatusUri"))).thenReturn(requestBodySpec)
        whenever(requestBodySpec.httpRequest(any())).thenReturn(requestBodySpec)
        whenever(requestBodySpec.retrieve()).thenReturn(responseSpec)
        whenever(
            responseSpec.toBodilessEntity(),
        ).thenReturn(Mono.just(ResponseEntity.created(URI.create("testCreatedUri")).build()))

        StepVerifier
            .create(fintArchiveDispatchClient.pollForCreatedLocationPublic(URI.create("testStatusUri")))
            .expectNext(URI.create("testCreatedUri"))
            .verifyComplete()

        verify(fintWebClient, times(1)).get()
    }

    @Test
    fun givenMaxPollingTimeIsExceededWhenPollForCreatedLocationShouldThrowException() {
        val requestHeadersUriSpec = mock<WebClient.RequestHeadersUriSpec<*>>()
        val requestBodySpec = mock<WebClient.RequestBodySpec>()
        val responseSpec = mock<WebClient.ResponseSpec>()
        whenever(fintWebClient.get()).thenReturn(requestHeadersUriSpec)
        whenever(requestHeadersUriSpec.uri(URI.create("testStatusUri"))).thenReturn(requestBodySpec)
        whenever(requestBodySpec.httpRequest(any())).thenReturn(requestBodySpec)
        whenever(requestBodySpec.retrieve()).thenReturn(responseSpec)
        whenever(responseSpec.toBodilessEntity()).thenReturn(Mono.just(ResponseEntity.noContent().build()))

        StepVerifier
            .create(fintArchiveDispatchClient.pollForCreatedLocationPublic(URI.create("testStatusUri")))
            .expectErrorSatisfies { error ->
                assert(error is CreatedLocationPollTimeoutException)
                assert(error.message!!.contains("statusUri=testStatusUri"))
                assert(error.message!!.contains("lastStatus=204 NO_CONTENT"))
            }.verify()

        verify(fintWebClient, atLeast(2)).get()
    }

    @Test
    fun givenErrorResponseWhenPollForCreatedLocationShouldThrowException() {
        val requestHeadersUriSpec = mock<WebClient.RequestHeadersUriSpec<*>>()
        val requestBodySpec = mock<WebClient.RequestBodySpec>()
        val responseSpec = mock<WebClient.ResponseSpec>()
        val webClientException = mock<WebClientException>()
        whenever(fintWebClient.get()).thenReturn(requestHeadersUriSpec)
        whenever(requestHeadersUriSpec.uri(URI.create("testStatusUri"))).thenReturn(requestBodySpec)
        whenever(requestBodySpec.httpRequest(any())).thenReturn(requestBodySpec)
        whenever(requestBodySpec.retrieve()).thenReturn(responseSpec)
        whenever(webClientException.message).thenReturn("test message")
        whenever(responseSpec.toBodilessEntity()).thenReturn(Mono.error(webClientException))

        StepVerifier
            .create(fintArchiveDispatchClient.pollForCreatedLocationPublic(URI.create("testStatusUri")))
            .expectErrorMessage("test message")
            .verify()
    }

    private class TestableFintArchiveDispatchClient(
        properties: FintArchiveDispatchClientConfigurationProperties,
        fintWebClient: WebClient,
        fintArchiveResourceClient: FintArchiveResourceClient,
        meterRegistry: MeterRegistry,
        webUtilErrorHandler: WebUtilErrorHandler,
    ) : FintArchiveDispatchClient(
        properties,
        fintWebClient,
        fintArchiveResourceClient,
        meterRegistry,
        webUtilErrorHandler,
    ) {
        fun pollForCreatedLocationPublic(statusUri: URI) = pollForCreatedLocation(statusUri)
    }
}
