package no.novari.flyt.archive.gateway.dispatch.file

import no.novari.flyt.archive.gateway.dispatch.DispatchStatus
import no.novari.flyt.archive.gateway.dispatch.file.result.FilesDispatchResult
import no.novari.flyt.archive.gateway.dispatch.model.instance.DokumentobjektDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class FilesDispatchService(
    private val fileDispatchService: FileDispatchService,
) {
    fun dispatch(dokumentobjektDtos: Collection<DokumentobjektDto>): Mono<FilesDispatchResult> {
        if (dokumentobjektDtos.isEmpty()) {
            log.info("No files to dispatch")
            return Mono.just(FilesDispatchResult.accepted(emptyMap()))
        }

        log.info("Dispatching files")
        return Flux
            .fromIterable(dokumentobjektDtos)
            .concatMap(fileDispatchService::dispatch)
            .takeUntil { fileDispatchResult -> fileDispatchResult.status != DispatchStatus.ACCEPTED }
            .collectList()
            .map { fileDispatchResults ->
                val lastResult = fileDispatchResults.last()
                when (lastResult.status) {
                    DispatchStatus.ACCEPTED -> {
                        FilesDispatchResult.accepted(
                            fileDispatchResults
                                .mapNotNull { fileDispatchResult ->
                                    val fileId = fileDispatchResult.fileId
                                    val archiveFileLink = fileDispatchResult.archiveFileLink
                                    if (fileId != null && archiveFileLink != null) {
                                        fileId to archiveFileLink
                                    } else {
                                        null
                                    }
                                }.toMap(),
                        )
                    }

                    DispatchStatus.DECLINED -> {
                        FilesDispatchResult.declined(lastResult.errorMessage.orEmpty())
                    }

                    DispatchStatus.FAILED -> {
                        FilesDispatchResult.failed()
                    }
                }
            }.doOnNext { result -> log.info("Dispatch result={}", result) }
    }

    companion object {
        private val log = LoggerFactory.getLogger(FilesDispatchService::class.java)
    }
}
