package no.novari.flyt.archive.gateway.resource.sak

import io.github.oshai.kotlinlogging.KotlinLogging
import no.novari.fint.model.resource.arkiv.noark.JournalpostResource
import no.novari.fint.model.resource.arkiv.noark.SakResource
import no.novari.flyt.archive.gateway.resource.web.FintArchiveResourceClient
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException

@Service
class CaseService(
    private val fintArchiveResourceClient: FintArchiveResourceClient,
) {
    fun getByMappeId(mappeId: String): SakResource? =
        try {
            findByMappeId(mappeId)
        } catch (error: RuntimeException) {
            log.atError {
                message = "Could not find case with id={}"
                arguments = arrayOf(mappeId)
                cause = error
            }
            null
        }

    fun getByArchiveInstanceIdWithFilteredJournalposts(archiveInstanceId: String): SakResource? =
        try {
            findByArchiveInstanceIdWithFilteredJournalposts(archiveInstanceId)
        } catch (error: RuntimeException) {
            log.atError {
                message = "Could not find case with archive instance id={}"
                arguments = arrayOf(archiveInstanceId)
                cause = error
            }
            null
        }

    /**
     * Returns null when FINT has no sak with the given mappeId. Other errors are thrown.
     */
    fun findByMappeId(mappeId: String): SakResource? =
        try {
            fintArchiveResourceClient.getResource("/arkiv/noark/sak/mappeid/$mappeId", SakResource::class.java)
        } catch (error: HttpClientErrorException) {
            if (error.statusCode == HttpStatus.NOT_FOUND) null else throw error
        }

    /**
     * Returns null when FINT has no sak for the given archive instance id. Other errors are thrown.
     *
     * @throws InvalidArchiveInstanceIdException if the archive instance id cannot be parsed
     */
    fun findByArchiveInstanceIdWithFilteredJournalposts(archiveInstanceId: String): SakResource? {
        val caseAndJournalpostIds = extractCaseAndJournalpostIds(archiveInstanceId)
        val sakResource = findByMappeId(caseAndJournalpostIds.caseId) ?: return null
        val journalposts: List<JournalpostResource> = sakResource.journalpost?.toList() ?: emptyList()
        sakResource.journalpost =
            journalposts.filter { journalpostResource ->
                caseAndJournalpostIds.journalpostIds.contains(journalpostResource.journalPostnummer)
            }
        return sakResource
    }

    /**
     * Accepts the formats written by [no.novari.flyt.archive.gateway.dispatch.DispatchMessageFormattingService]:
     * `caseId` or `caseId-[1,2]`.
     */
    private fun extractCaseAndJournalpostIds(archiveInstanceId: String): CaseAndJournalpostIds {
        val match =
            ARCHIVE_INSTANCE_ID_PATTERN.matchEntire(archiveInstanceId)
                ?: throw InvalidArchiveInstanceIdException(archiveInstanceId)
        val (caseId, journalpostIdList) = match.destructured
        val journalpostIds =
            journalpostIdList
                .split(",")
                .filter(String::isNotEmpty)
                .map { it.toLongOrNull() ?: throw InvalidArchiveInstanceIdException(archiveInstanceId) }

        return CaseAndJournalpostIds(caseId = caseId, journalpostIds = journalpostIds)
    }

    private data class CaseAndJournalpostIds(
        val caseId: String,
        val journalpostIds: List<Long>,
    )

    companion object {
        private val log = KotlinLogging.logger {}
        private val ARCHIVE_INSTANCE_ID_PATTERN = Regex("""([^-\[\]]+)(?:-\[(\d+(?:,\d+)*)])?""")
    }
}
