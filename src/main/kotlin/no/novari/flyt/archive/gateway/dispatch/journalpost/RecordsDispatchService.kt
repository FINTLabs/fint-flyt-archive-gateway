package no.novari.flyt.archive.gateway.dispatch.journalpost

import no.novari.flyt.archive.gateway.dispatch.DispatchMessageFormattingService
import no.novari.flyt.archive.gateway.dispatch.DispatchStatus
import no.novari.flyt.archive.gateway.dispatch.journalpost.result.RecordDispatchResult
import no.novari.flyt.archive.gateway.dispatch.journalpost.result.RecordsDispatchResult
import no.novari.flyt.archive.gateway.dispatch.model.instance.JournalpostDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class RecordsDispatchService(
    private val recordDispatchService: RecordDispatchService,
    private val dispatchMessageFormattingService: DispatchMessageFormattingService,
) {
    fun dispatch(
        caseId: String,
        journalpostDtos: List<JournalpostDto>,
    ): RecordsDispatchResult {
        log.info("Dispatching records")
        if (journalpostDtos.isEmpty()) {
            return RecordsDispatchResult.accepted(emptyList())
        }

        val recordDispatchResults = mutableListOf<RecordDispatchResult>()
        val result =
            try {
                for (journalpostDto in journalpostDtos) {
                    val recordResult = recordDispatchService.dispatch(caseId, journalpostDto)
                    recordDispatchResults += recordResult
                    if (recordResult.status != DispatchStatus.ACCEPTED) {
                        break
                    }
                }
                val lastResult: RecordDispatchResult = recordDispatchResults.last()
                val lastStatus = lastResult.status

                val idsOfSuccessfullyDispatchedRecords =
                    (
                        if (lastStatus == DispatchStatus.ACCEPTED) {
                            recordDispatchResults
                        } else {
                            recordDispatchResults.subList(0, recordDispatchResults.size - 1)
                        }
                    ).mapNotNull(RecordDispatchResult::journalpostId)

                when (lastStatus) {
                    DispatchStatus.ACCEPTED -> {
                        RecordsDispatchResult.accepted(idsOfSuccessfullyDispatchedRecords)
                    }

                    DispatchStatus.DECLINED -> {
                        RecordsDispatchResult.declined(
                            lastResult.errorMessage.orEmpty(),
                            createFunctionalWarningMessages(idsOfSuccessfullyDispatchedRecords),
                        )
                    }

                    DispatchStatus.FAILED -> {
                        RecordsDispatchResult.failed(
                            lastResult.errorMessage.orEmpty(),
                            createFunctionalWarningMessages(idsOfSuccessfullyDispatchedRecords),
                        )
                    }
                }
            } catch (error: Throwable) {
                log.error("Journalposts dispatch failed", error)
                RecordsDispatchResult.failed(
                    "Journalposts dispatch failed",
                    "possible journalposts with unknown ids",
                )
            }
        log.info("Dispatch result={}", result)
        return result
    }

    private fun createFunctionalWarningMessages(idsOfSuccessfullyDispatchedRecords: List<Long>): String? =
        dispatchMessageFormattingService.createFunctionalWarningMessageOrNull(
            "journalpost",
            "id",
            idsOfSuccessfullyDispatchedRecords.map(Long::toString),
        )

    companion object {
        private val log = LoggerFactory.getLogger(RecordsDispatchService::class.java)
    }
}
