package no.novari.flyt.archive.gateway

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.mock.http.client.MockClientHttpRequest
import org.springframework.mock.http.client.MockClientHttpResponse
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestClient
import java.io.IOException
import java.net.ConnectException
import java.net.URI
import java.net.UnknownHostException
import java.net.http.HttpTimeoutException
import java.security.cert.CertificateExpiredException
import javax.net.ssl.SSLException
import javax.net.ssl.SSLHandshakeException

class FintArchiveRequestLoggingInterceptorTest {
    private val interceptor = FintArchiveRequestLoggingInterceptor()
    private val logger = LoggerFactory.getLogger(FintArchiveRequestLoggingInterceptor::class.java) as Logger
    private val appender = ListAppender<ILoggingEvent>()
    private var previousLevel: Level? = null

    @BeforeEach
    fun setup() {
        previousLevel = logger.level
        logger.level = Level.DEBUG
        appender.start()
        logger.addAppender(appender)
    }

    @AfterEach
    fun cleanup() {
        logger.detachAppender(appender)
        appender.stop()
        logger.level = previousLevel
    }

    @Test
    fun `logs gateway response metadata without exposing credentials query or bodies`() {
        val request =
            MockClientHttpRequest(
                HttpMethod.POST,
                URI.create("https://user:password@archive.example/arkiv/noark/sak/\$query?filter=secret-filter"),
            )
        request.headers.setBearerAuth("secret-token")
        val response = MockClientHttpResponse("secret-response".toByteArray(), HttpStatus.BAD_GATEWAY)
        response.headers.set("Server", "Access Gateway")
        response.headers.set("X-Request-ID", "proxy-request-123")
        response.headers.set("Set-Cookie", "secret-cookie")
        val execution = mock<ClientHttpRequestExecution>()
        whenever(execution.execute(any(), any())).thenReturn(response)

        val result = interceptor.intercept(request, "secret-request".toByteArray(), execution)

        assertThat(result).isSameAs(response)
        assertThat(result.body.bufferedReader().readText()).isEqualTo("secret-response")
        assertThat(messages())
            .contains(
                "method=POST",
                "endpoint=https://archive.example/arkiv/noark/sak/\$query",
                "status=502",
                "elapsedMs=",
            ).contains("Server=Access Gateway", "X-Request-ID=proxy-request-123")
            .doesNotContain("user", "password", "secret-", "Set-Cookie", "filter=")
        assertThat(appender.list.last().level).isEqualTo(Level.WARN)
    }

    @Test
    fun `classifies transport failures and rethrows the original exception without logging its message`() {
        val failures =
            listOf(
                SSLHandshakeException("secret-handshake").apply {
                    initCause(CertificateExpiredException("secret-certificate"))
                } to "CERTIFICATE",
                SSLHandshakeException("secret-handshake") to "TLS_HANDSHAKE",
                SSLException("secret-tls") to "TLS",
                HttpTimeoutException("secret-timeout") to "TIMEOUT",
                UnknownHostException("secret-host") to "DNS",
                ConnectException("secret-connect") to "CONNECT",
            )
        failures.forEach { (cause, expectedType) ->
            appender.list.clear()
            val error = IOException("secret-wrapper", cause)
            val execution = mock<ClientHttpRequestExecution>()
            whenever(execution.execute(any(), any())).thenThrow(error)

            assertThatThrownBy {
                interceptor.intercept(
                    MockClientHttpRequest(
                        HttpMethod.POST,
                        URI.create("https://archive.example/arkiv/noark/sak/\$query"),
                    ),
                    byteArrayOf(),
                    execution,
                )
            }.isSameAs(error)

            assertThat(messages())
                .contains("failureType=$expectedType", cause.javaClass.name, "elapsedMs=")
                .doesNotContain("secret-", "status=502")
            assertThat(appender.list.last().throwableProxy).isNull()
        }
    }

    @Test
    fun `leaves response body available for RestClient status handling`() {
        val builder = RestClient.builder().requestInterceptor(interceptor)
        val server = MockRestServiceServer.bindTo(builder).build()
        server
            .expect(requestTo("https://archive.example/arkiv/noark/sak/\$query"))
            .andRespond(withStatus(HttpStatus.BAD_GATEWAY).body("gateway error body").contentType(MediaType.TEXT_HTML))

        assertThatThrownBy {
            builder
                .build()
                .post()
                .uri("https://archive.example/arkiv/noark/sak/\$query")
                .retrieve()
                .body(String::class.java)
        }.isInstanceOfSatisfying(HttpServerErrorException.BadGateway::class.java) {
            assertThat(it.responseBodyAsString).isEqualTo("gateway error body")
        }
        server.verify()
        assertThat(messages()).contains("status=502", "Content-Type=text/html").doesNotContain("gateway error body")
    }

    private fun messages(): String = appender.list.joinToString("\n") { it.formattedMessage }
}
