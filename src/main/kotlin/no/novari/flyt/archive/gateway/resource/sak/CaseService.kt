package no.novari.flyt.archive.gateway.resource.sak

import io.github.oshai.kotlinlogging.KotlinLogging
import no.novari.fint.model.resource.arkiv.noark.JournalpostResource
import no.novari.fint.model.resource.arkiv.noark.SakResource
import no.novari.flyt.archive.gateway.resource.web.FintArchiveResourceClient
import org.springframework.stereotype.Service

@Service
class CaseService(
    private val fintArchiveResourceClient: FintArchiveResourceClient,
) {
    fun getByMappeId(mappeId: String): SakResource? =
        try {
            fetchByMappeId(mappeId)
        } catch (error: RuntimeException) {
            log.atError {
                message = "Could not find case with id={}"
                arguments = arrayOf(mappeId)
                cause = error
            }
            null
        }

    fun getByArchiveInstanceIdWithFilteredJournalposts(archiveInstanceId: String): SakResource? {
        val caseAndJournalpostIds = extractCaseAndJournalpostIds(archiveInstanceId)
        return try {
            val sakResource = fetchByMappeId(caseAndJournalpostIds.caseId)
            val journalposts: List<JournalpostResource> = sakResource.journalpost?.toList() ?: emptyList()
            sakResource.journalpost =
                journalposts.filter { journalpostResource ->
                    caseAndJournalpostIds.journalpostIds.contains(journalpostResource.journalPostnummer)
                }
            sakResource
        } catch (error: RuntimeException) {
            log.atError {
                message = "Could not find case with archive instance id={}"
                arguments = arrayOf(archiveInstanceId)
                cause = error
            }
            null
        }
    }

    private fun fetchByMappeId(mappeId: String): SakResource =
        fintArchiveResourceClient.getResource("/arkiv/noark/sak/mappeid/$mappeId", SakResource::class.java)

    private fun extractCaseAndJournalpostIds(archiveInstanceId: String): CaseAndJournalpostIds {
        val splitArchiveInstanceId = archiveInstanceId.split("-")
        val caseId = splitArchiveInstanceId.first()
        val journalpostIds =
            if (splitArchiveInstanceId.size == 1) {
                emptyList()
            } else {
                splitArchiveInstanceId[1]
                    .replace("[", "")
                    .replace("]", "")
                    .split(",")
                    .filter(String::isNotBlank)
                    .map(String::toLong)
            }

        return CaseAndJournalpostIds(caseId = caseId, journalpostIds = journalpostIds)
    }

    private data class CaseAndJournalpostIds(
        val caseId: String,
        val journalpostIds: List<Long>,
    )

    companion object {
        private val log = KotlinLogging.logger {}
    }
}
