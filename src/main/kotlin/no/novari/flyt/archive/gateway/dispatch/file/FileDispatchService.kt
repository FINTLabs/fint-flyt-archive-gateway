package no.novari.flyt.archive.gateway.dispatch.file

import io.netty.handler.timeout.ReadTimeoutException
import no.novari.flyt.archive.gateway.dispatch.file.result.FileDispatchResult
import no.novari.flyt.archive.gateway.dispatch.model.instance.DokumentobjektDto
import no.novari.flyt.archive.gateway.dispatch.web.CreatedLocationPollTimeoutException
import no.novari.flyt.archive.gateway.dispatch.web.FintArchiveDispatchClient
import no.novari.flyt.archive.gateway.dispatch.web.flytfile.FlytFileClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono

@Service
class FileDispatchService(
    private val fintArchiveDispatchClient: FintArchiveDispatchClient,
    private val flytFileClient: FlytFileClient,
) {
    fun dispatch(dokumentobjektDto: DokumentobjektDto): Mono<FileDispatchResult> {
        log.info("Dispatching file")
        val fileId =
            dokumentobjektDto.fileId
                ?: return Mono.just(FileDispatchResult.noFileId()).doOnNext { result ->
                    log.info("Dispatch result={}", result)
                }

        return flytFileClient
            .getFile(fileId)
            .flatMap { file ->
                fintArchiveDispatchClient
                    .postFile(file)
                    .map { link -> FileDispatchResult.accepted(fileId, link) }
                    .onErrorResume(WebClientResponseException::class.java) { error ->
                        Mono.just(FileDispatchResult.declined(fileId, error.responseBodyAsString))
                    }.onErrorResume({ error ->
                        error is ReadTimeoutException || error is CreatedLocationPollTimeoutException
                    }) {
                        log.error("File dispatch timed out", it)
                        Mono.just(FileDispatchResult.timedOut(fileId))
                    }.onErrorResume {
                        log.error("File dispatch failed", it)
                        Mono.just(FileDispatchResult.failed(fileId))
                    }
            }.onErrorResume {
                log.error("File could not be retrieved", it)
                Mono.just(FileDispatchResult.couldNotBeRetrieved(fileId))
            }.doOnNext { result -> log.info("Dispatch result={}", result) }
    }

    companion object {
        private val log = LoggerFactory.getLogger(FileDispatchService::class.java)
    }
}
