package no.novari.flyt.archive.gateway.dispatch.web

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import no.novari.flyt.archive.gateway.WebUtilErrorHandler
import no.novari.flyt.archive.gateway.resource.web.FintArchiveResourceClient
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.atLeast
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.ResponseEntity
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClient
import java.net.URI
import java.time.Duration

class FintArchiveDispatchClientTest {
    private lateinit var fintRestClient: RestClient
    private lateinit var fintArchiveResourceClient: FintArchiveResourceClient
    private lateinit var fintArchiveDispatchClient: TestableFintArchiveDispatchClient
    private lateinit var meterRegistry: MeterRegistry
    private lateinit var webUtilErrorHandler: WebUtilErrorHandler

    @BeforeEach
    fun setup() {
        fintRestClient = mock()
        fintArchiveResourceClient = mock()
        meterRegistry = SimpleMeterRegistry()
        webUtilErrorHandler = mock()
        val properties =
            FintArchiveDispatchClientConfigurationProperties().apply {
                createdLocationPollBackoffMinDelay = Duration.ofMillis(100L)
                createdLocationPollBackoffMaxDelay = Duration.ofMillis(250L)
                createdLocationPollTotalTimeout = Duration.ofMillis(1000L)
            }
        fintArchiveDispatchClient =
            TestableFintArchiveDispatchClient(
                properties,
                fintRestClient,
                fintArchiveResourceClient,
                meterRegistry,
                webUtilErrorHandler,
            )
    }

    @Test
    fun `given a created location on the first poll, returns without repeating`() {
        mockGetReturning(ResponseEntity.created(URI.create("testCreatedUri")).build())

        val result = fintArchiveDispatchClient.pollForCreatedLocationPublic(URI.create("testStatusUri"))

        assertThat(result).isEqualTo(URI.create("testCreatedUri"))
        verify(fintRestClient, times(1)).get()
    }

    @Test
    fun `given the max polling time is exceeded, throws CreatedLocationPollTimeoutException`() {
        mockGetReturning(ResponseEntity.noContent().build())

        assertThatThrownBy { fintArchiveDispatchClient.pollForCreatedLocationPublic(URI.create("testStatusUri")) }
            .isInstanceOf(CreatedLocationPollTimeoutException::class.java)
            .hasMessageContaining("statusUri=testStatusUri")
            .hasMessageContaining("lastStatus=204 NO_CONTENT")

        verify(fintRestClient, atLeast(2)).get()
    }

    @Test
    fun `given an error response, propagates the exception`() {
        mockGetThrowing(ResourceAccessException("test message"))

        assertThatThrownBy { fintArchiveDispatchClient.pollForCreatedLocationPublic(URI.create("testStatusUri")) }
            .isInstanceOf(ResourceAccessException::class.java)
            .hasMessage("test message")
    }

    private fun mockGetReturning(response: ResponseEntity<Void>) {
        val uriSpec = mock<RestClient.RequestHeadersUriSpec<*>>()
        val headersSpec = mock<RestClient.RequestHeadersSpec<*>>()
        val responseSpec = mock<RestClient.ResponseSpec>()
        whenever(fintRestClient.get()).thenReturn(uriSpec)
        doReturn(headersSpec).whenever(uriSpec).uri(any<URI>())
        whenever(headersSpec.retrieve()).thenReturn(responseSpec)
        whenever(responseSpec.toBodilessEntity()).thenReturn(response)
    }

    private fun mockGetThrowing(error: Throwable) {
        val uriSpec = mock<RestClient.RequestHeadersUriSpec<*>>()
        val headersSpec = mock<RestClient.RequestHeadersSpec<*>>()
        val responseSpec = mock<RestClient.ResponseSpec>()
        whenever(fintRestClient.get()).thenReturn(uriSpec)
        doReturn(headersSpec).whenever(uriSpec).uri(any<URI>())
        whenever(headersSpec.retrieve()).thenReturn(responseSpec)
        doThrow(error).whenever(responseSpec).toBodilessEntity()
    }

    private class TestableFintArchiveDispatchClient(
        properties: FintArchiveDispatchClientConfigurationProperties,
        fintRestClient: RestClient,
        fintArchiveResourceClient: FintArchiveResourceClient,
        meterRegistry: MeterRegistry,
        webUtilErrorHandler: WebUtilErrorHandler,
    ) : FintArchiveDispatchClient(
            properties,
            fintRestClient,
            fintArchiveResourceClient,
            meterRegistry,
            webUtilErrorHandler,
        ) {
        fun pollForCreatedLocationPublic(statusUri: URI) = pollForCreatedLocation(statusUri)
    }
}
