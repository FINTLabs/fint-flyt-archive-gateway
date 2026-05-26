package no.novari.flyt.archive.gateway.dispatch.file

import no.novari.flyt.archive.gateway.dispatch.file.result.FileDispatchResult
import no.novari.flyt.archive.gateway.dispatch.isReadTimeout
import no.novari.flyt.archive.gateway.dispatch.model.instance.DokumentobjektDto
import no.novari.flyt.archive.gateway.dispatch.web.CreatedLocationPollTimeoutException
import no.novari.flyt.archive.gateway.dispatch.web.FintArchiveDispatchClient
import no.novari.flyt.archive.gateway.dispatch.web.flytfile.FlytFileClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientResponseException

@Service
class FileDispatchService(
    private val fintArchiveDispatchClient: FintArchiveDispatchClient,
    private val flytFileClient: FlytFileClient,
) {
    fun dispatch(dokumentobjektDto: DokumentobjektDto): FileDispatchResult {
        log.info("Dispatching file")
        val fileId =
            dokumentobjektDto.fileId
                ?: return FileDispatchResult.noFileId().also { result ->
                    log.info("Dispatch result={}", result)
                }

        val file =
            try {
                flytFileClient.getFile(fileId)
            } catch (error: Throwable) {
                log.error("File could not be retrieved", error)
                return FileDispatchResult.couldNotBeRetrieved(fileId).also { log.info("Dispatch result={}", it) }
            }

        val result =
            try {
                val link = fintArchiveDispatchClient.postFile(file)
                FileDispatchResult.accepted(fileId, link)
            } catch (error: RestClientResponseException) {
                FileDispatchResult.declined(fileId, error.responseBodyAsString)
            } catch (error: CreatedLocationPollTimeoutException) {
                log.error("File dispatch timed out", error)
                FileDispatchResult.timedOut(fileId)
            } catch (error: Throwable) {
                if (isReadTimeout(error)) {
                    log.error("File dispatch timed out", error)
                    FileDispatchResult.timedOut(fileId)
                } else {
                    log.error("File dispatch failed", error)
                    FileDispatchResult.failed(fileId)
                }
            }
        log.info("Dispatch result={}", result)
        return result
    }

    companion object {
        private val log = LoggerFactory.getLogger(FileDispatchService::class.java)
    }
}
