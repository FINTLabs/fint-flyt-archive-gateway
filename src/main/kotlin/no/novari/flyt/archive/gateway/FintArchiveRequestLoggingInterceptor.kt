package no.novari.flyt.archive.gateway

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.URI
import java.net.UnknownHostException
import java.net.http.HttpTimeoutException
import java.security.cert.CertPathBuilderException
import java.security.cert.CertPathValidatorException
import java.security.cert.CertificateException
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLException
import javax.net.ssl.SSLHandshakeException

class FintArchiveRequestLoggingInterceptor : ClientHttpRequestInterceptor {
    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution,
    ): ClientHttpResponse {
        val requestId = UUID.randomUUID()
        val endpoint = URI(request.uri.scheme, null, request.uri.host, request.uri.port, request.uri.path, null, null)
        val startedAt = System.nanoTime()
        log.debug { "FINT archive request: requestId=$requestId, method=${request.method}, endpoint=$endpoint" }

        val response =
            try {
                execution.execute(request, body)
            } catch (error: Exception) {
                val causes = generateSequence<Throwable>(error) { it.cause }.take(10).toList()
                log.atWarn {
                    message =
                        "FINT archive request failed before receiving a response: " +
                        "requestId={}, method={}, endpoint={}, elapsedMs={}, failureType={}, causeTypes={}"
                    arguments =
                        arrayOf(
                            requestId,
                            request.method,
                            endpoint,
                            elapsedMillis(startedAt),
                            failureType(causes),
                            causes.map { it.javaClass.name },
                        )
                }
                throw error
            }

        try {
            val message =
                "FINT archive response: requestId=$requestId, method=${request.method}, endpoint=$endpoint, " +
                    "elapsedMs=${elapsedMillis(startedAt)}, status=${response.statusCode.value()}, " +
                    "responseHeaders=${diagnosticHeaders(response.headers)}"
            if (response.statusCode.isError) {
                log.warn { message }
            } else {
                log.debug { message }
            }
            return response
        } catch (error: Exception) {
            response.close()
            throw error
        }
    }

    private fun elapsedMillis(startedAt: Long): Long = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt)

    private fun diagnosticHeaders(headers: HttpHeaders): Map<String, String> =
        responseHeaderNames
            .mapNotNull { name ->
                headers.getFirst(name)?.let { value ->
                    name to value.take(256).map { if (it.isISOControl()) ' ' else it }.joinToString("")
                }
            }.toMap()

    private fun failureType(causes: List<Throwable>): String {
        val certificateFailure =
            causes.any {
                it is CertificateException || it is CertPathBuilderException || it is CertPathValidatorException
            }
        return when {
            certificateFailure -> "CERTIFICATE"
            causes.any { it is SSLHandshakeException } -> "TLS_HANDSHAKE"
            causes.any { it is SSLException } -> "TLS"
            causes.any { it is HttpTimeoutException || it is SocketTimeoutException } -> "TIMEOUT"
            causes.any { it is UnknownHostException } -> "DNS"
            causes.any { it is ConnectException } -> "CONNECT"
            else -> "OTHER"
        }
    }

    companion object {
        private val log = KotlinLogging.logger {}
        private val responseHeaderNames =
            listOf("Content-Type", "Server", "Via", "X-Request-ID", "X-Correlation-ID", "traceparent")
    }
}
