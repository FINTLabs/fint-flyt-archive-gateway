package no.fintlabs.flyt.gateway.application.archive.dispatch.web.flytfile;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.flyt.gateway.application.archive.dispatch.model.File;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClientRequest;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
public class FlytFileClient {

    private final WebClient fileWebClient;
    private final Long timeoutMillis;

    public FlytFileClient(
            @Qualifier("fileWebClient") WebClient fileWebClient,
            @Value("fint.flyt.gateway.application.archive.dispatch.flyt-file-client.get-file-timeout-millis")
            Long timeoutMillis
    ) {
        this.fileWebClient = fileWebClient;
        this.timeoutMillis = timeoutMillis;
    }

    public Mono<File> getFile(UUID fileId) {
        log.info("Getting file");
        return fileWebClient
                .get()
                .uri("/" + fileId)
                .httpRequest(clientHttpRequest -> {
                    HttpClientRequest reactorRequest = clientHttpRequest.getNativeRequest();
                    reactorRequest.responseTimeout(Duration.ofMillis(timeoutMillis));
                })
                .retrieve()
                .bodyToMono(File.class)
                .retryWhen(Retry
                        .backoff(5, Duration.ofSeconds(1))
                        .doBeforeRetry(retrySignal -> log.warn(
                                "Could not retrieve file with id=" + fileId +
                                " -- performing retry " + (retrySignal.totalRetries() + 1),
                                retrySignal.failure())
                        )
                )
                .doOnNext(file -> log.info("Retrieved file with id=" + fileId))
                .doOnError(e -> log.error("Could not retrieve file with id=" + fileId, e));
    }

}
