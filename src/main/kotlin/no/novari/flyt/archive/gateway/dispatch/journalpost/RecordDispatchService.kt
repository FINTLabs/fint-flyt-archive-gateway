package no.novari.flyt.archive.gateway.dispatch.journalpost

import no.novari.fint.model.resource.Link
import no.novari.fint.model.resource.arkiv.noark.JournalpostResource
import no.novari.flyt.archive.gateway.dispatch.DispatchStatus
import no.novari.flyt.archive.gateway.dispatch.file.FilesDispatchService
import no.novari.flyt.archive.gateway.dispatch.isReadTimeout
import no.novari.flyt.archive.gateway.dispatch.journalpost.result.RecordDispatchResult
import no.novari.flyt.archive.gateway.dispatch.mapping.JournalpostMappingService
import no.novari.flyt.archive.gateway.dispatch.model.instance.DokumentbeskrivelseDto
import no.novari.flyt.archive.gateway.dispatch.model.instance.DokumentobjektDto
import no.novari.flyt.archive.gateway.dispatch.model.instance.JournalpostDto
import no.novari.flyt.archive.gateway.dispatch.web.CreatedLocationPollTimeoutException
import no.novari.flyt.archive.gateway.dispatch.web.FintArchiveDispatchClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientResponseException
import java.util.UUID

@Service
class RecordDispatchService(
    private val journalpostMappingService: JournalpostMappingService,
    private val filesDispatchService: FilesDispatchService,
    private val fintArchiveDispatchClient: FintArchiveDispatchClient,
) {
    fun dispatch(
        caseId: String,
        journalpostDto: JournalpostDto,
    ): RecordDispatchResult {
        log.info("Dispatching record")

        val dokumentobjektDtos = journalpostDto.dokumentbeskrivelse?.flatMap(this::getDokumentObjektDtos).orEmpty()
        val result =
            if (dokumentobjektDtos.isEmpty()) {
                dispatch(caseId, journalpostDto, emptyMap())
            } else {
                val filesDispatchResult = filesDispatchService.dispatch(dokumentobjektDtos)
                when (filesDispatchResult.status) {
                    DispatchStatus.ACCEPTED -> {
                        dispatch(caseId, journalpostDto, filesDispatchResult.archiveFileLinkPerFileId.orEmpty())
                    }

                    DispatchStatus.DECLINED -> {
                        RecordDispatchResult.declined(
                            "Dokumentobjekt declined by destination with message='${filesDispatchResult.errorMessage}'",
                        )
                    }

                    DispatchStatus.FAILED -> {
                        RecordDispatchResult.failed("Dokumentobjekt dispatch failed")
                    }
                }
            }
        log.info("Dispatch result={}", result)
        return result
    }

    private fun getDokumentObjektDtos(dokumentbeskrivelseDto: DokumentbeskrivelseDto): Collection<DokumentobjektDto> =
        dokumentbeskrivelseDto.dokumentobjekt.orEmpty()

    private fun dispatch(
        caseId: String,
        journalpostDto: JournalpostDto,
        archiveFileLinkPerFileId: Map<UUID, Link>,
    ): RecordDispatchResult {
        val journalpostResource: JournalpostResource =
            journalpostMappingService.toJournalpostResource(journalpostDto, archiveFileLinkPerFileId)

        return try {
            val resultJournalpost = fintArchiveDispatchClient.postRecord(caseId, journalpostResource)
            RecordDispatchResult.accepted(resultJournalpost.journalPostnummer)
        } catch (error: RestClientResponseException) {
            RecordDispatchResult.declined(error.responseBodyAsString)
        } catch (error: CreatedLocationPollTimeoutException) {
            log.error("Record dispatch timed out", error)
            RecordDispatchResult.timedOut()
        } catch (error: Throwable) {
            if (isReadTimeout(error)) {
                log.error("Record dispatch timed out", error)
                RecordDispatchResult.timedOut()
            } else {
                log.error("Failed to post record", error)
                RecordDispatchResult.failed("Failed to post record")
            }
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(RecordDispatchService::class.java)
    }
}
