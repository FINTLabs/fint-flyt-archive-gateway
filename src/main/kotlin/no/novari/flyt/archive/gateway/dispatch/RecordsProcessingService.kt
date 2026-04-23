package no.novari.flyt.archive.gateway.dispatch

import no.novari.flyt.archive.gateway.dispatch.journalpost.RecordsDispatchService
import no.novari.flyt.archive.gateway.dispatch.model.instance.JournalpostDto
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class RecordsProcessingService(
    private val recordsDispatchService: RecordsDispatchService,
    private val dispatchMessageFormattingService: DispatchMessageFormattingService,
) {
    fun processRecords(
        archiveCaseId: String,
        newCase: Boolean,
        journalpostDtos: List<JournalpostDto>,
    ): Mono<DispatchResult> =
        recordsDispatchService.dispatch(archiveCaseId, journalpostDtos).map { recordsDispatchResult ->
            val functionalWarningMessages =
                recordsDispatchResult.functionalWarningMessage?.let(::listOf) ?: emptyList()

            when (recordsDispatchResult.status) {
                DispatchStatus.ACCEPTED -> {
                    DispatchResult.accepted(
                        dispatchMessageFormattingService.formatCaseIdAndJournalpostIds(
                            archiveCaseId,
                            recordsDispatchResult.journalpostIds.orEmpty(),
                        ),
                    )
                }

                DispatchStatus.DECLINED -> {
                    DispatchResult.declined(
                        "Journalpost was declined by the destination. " +
                            dispatchMessageFormattingService
                                .combineFunctionalWarningMessagesOrNull(
                                    archiveCaseId,
                                    newCase,
                                    functionalWarningMessages,
                                )?.let { " $it " }
                                .orEmpty() +
                            "Error message: '${recordsDispatchResult.errorMessage}'",
                    )
                }

                DispatchStatus.FAILED -> {
                    DispatchResult.failed(
                        "Journalpost dispatch failed." +
                            dispatchMessageFormattingService
                                .combineFunctionalWarningMessagesOrNull(
                                    archiveCaseId,
                                    newCase,
                                    functionalWarningMessages,
                                )?.let { " $it" }
                                .orEmpty(),
                    )
                }
            }
        }
}
