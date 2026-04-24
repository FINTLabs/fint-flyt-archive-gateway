package no.novari.flyt.archive.gateway.dispatch.web.flytfile

import no.novari.flyt.archive.gateway.dispatch.model.File
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClientRequest
import reactor.util.retry.Retry
import java.time.Duration
import java.util.UUID

@Service
class FlytFileClient(
    @param:Qualifier("fileWebClient")
    private val fileWebClient: WebClient,
    @param:Value("\${novari.flyt.archive.gateway.dispatch.flyt-file-client.get-file-timeout}")
    private val timeout: Duration,
) {
    fun getFile(fileId: UUID): Mono<File> {
        log.info("Getting file")
        return fileWebClient
            .get()
            .uri("/$fileId")
            .httpRequest { clientHttpRequest ->
                val reactorRequest = clientHttpRequest.getNativeRequest<HttpClientRequest>()
                reactorRequest.responseTimeout(timeout)
            }.retrieve()
            .bodyToMono(File::class.java)
            .retryWhen(
                Retry
                    .backoff(5, Duration.ofSeconds(1))
                    .doBeforeRetry { retrySignal ->
                        log.warn(
                            "Could not retrieve file with id={} -- performing retry {}",
                            fileId,
                            retrySignal.totalRetries() + 1,
                            retrySignal.failure(),
                        )
                    },
            ).doOnNext { log.info("Retrieved file with id={}", fileId) }
            .doOnError { error -> log.error("Could not retrieve file with id={}", fileId, error) }
    }

    companion object {
        private val log = LoggerFactory.getLogger(FlytFileClient::class.java)
    }
}
