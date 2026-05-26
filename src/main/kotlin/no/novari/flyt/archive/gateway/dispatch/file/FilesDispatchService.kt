package no.novari.flyt.archive.gateway.dispatch.file

import no.novari.flyt.archive.gateway.dispatch.DispatchStatus
import no.novari.flyt.archive.gateway.dispatch.file.result.FileDispatchResult
import no.novari.flyt.archive.gateway.dispatch.file.result.FilesDispatchResult
import no.novari.flyt.archive.gateway.dispatch.model.instance.DokumentobjektDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class FilesDispatchService(
    private val fileDispatchService: FileDispatchService,
) {
    fun dispatch(dokumentobjektDtos: Collection<DokumentobjektDto>): FilesDispatchResult {
        if (dokumentobjektDtos.isEmpty()) {
            log.info("No files to dispatch")
            return FilesDispatchResult.accepted(emptyMap())
        }

        log.info("Dispatching files")
        val fileDispatchResults = mutableListOf<FileDispatchResult>()
        for (dokumentobjektDto in dokumentobjektDtos) {
            val result = fileDispatchService.dispatch(dokumentobjektDto)
            fileDispatchResults += result
            if (result.status != DispatchStatus.ACCEPTED) {
                break
            }
        }

        val lastResult = fileDispatchResults.last()
        val result =
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
        log.info("Dispatch result={}", result)
        return result
    }

    companion object {
        private val log = LoggerFactory.getLogger(FilesDispatchService::class.java)
    }
}
