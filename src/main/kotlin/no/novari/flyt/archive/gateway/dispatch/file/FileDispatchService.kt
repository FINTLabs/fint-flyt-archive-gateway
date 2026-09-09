package no.novari.flyt.archive.gateway.dispatch.file

import io.github.oshai.kotlinlogging.KotlinLogging
import no.novari.flyt.archive.gateway.dispatch.file.result.FileDispatchResult
import no.novari.flyt.archive.gateway.dispatch.isReadTimeout
import no.novari.flyt.archive.gateway.dispatch.model.instance.DokumentobjektDto
import no.novari.flyt.archive.gateway.dispatch.web.CreatedLocationPollTimeoutException
import no.novari.flyt.archive.gateway.dispatch.web.FintArchiveDispatchClient
import no.novari.flyt.archive.gateway.dispatch.web.flytfile.FlytFileClient
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientResponseException

@Service
class FileDispatchService(
    private val fintArchiveDispatchClient: FintArchiveDispatchClient,
    private val flytFileClient: FlytFileClient,
) {
    fun dispatch(dokumentobjektDto: DokumentobjektDto): FileDispatchResult {
        log.info { "Dispatching file" }
        val fileId =
            dokumentobjektDto.fileId
                ?: return FileDispatchResult.noFileId().also { result ->
                    log.atInfo {
                        message = "Dispatch result={}"
                        arguments = arrayOf(result)
                    }
                }

        val file =
            try {
                flytFileClient.getFile(fileId)
            } catch (error: Throwable) {
                log.atError {
                    message = "File could not be retrieved"
                    cause = error
                }
                return FileDispatchResult.couldNotBeRetrieved(fileId).also {
                    log.atInfo {
                        message = "Dispatch result={}"
                        arguments = arrayOf(it)
                    }
                }
            }

        val result =
            try {
                val link = fintArchiveDispatchClient.postFile(file)
                FileDispatchResult.accepted(fileId, link)
            } catch (error: RestClientResponseException) {
                FileDispatchResult.declined(fileId, error.responseBodyAsString)
            } catch (error: CreatedLocationPollTimeoutException) {
                log.atError {
                    message = "File dispatch timed out"
                    cause = error
                }
                FileDispatchResult.timedOut(fileId)
            } catch (error: Throwable) {
                if (isReadTimeout(error)) {
                    log.atError {
                        message = "File dispatch timed out"
                        cause = error
                    }
                    FileDispatchResult.timedOut(fileId)
                } else {
                    log.atError {
                        message = "File dispatch failed"
                        cause = error
                    }
                    FileDispatchResult.failed(fileId)
                }
            }
        log.atInfo {
            message = "Dispatch result={}"
            arguments = arrayOf(result)
        }
        return result
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}
