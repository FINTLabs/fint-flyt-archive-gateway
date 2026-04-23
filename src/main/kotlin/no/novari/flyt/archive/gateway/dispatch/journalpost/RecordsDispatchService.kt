package no.novari.flyt.archive.gateway.dispatch.journalpost

import no.novari.flyt.archive.gateway.dispatch.DispatchMessageFormattingService
import no.novari.flyt.archive.gateway.dispatch.DispatchStatus
import no.novari.flyt.archive.gateway.dispatch.journalpost.result.RecordDispatchResult
import no.novari.flyt.archive.gateway.dispatch.journalpost.result.RecordsDispatchResult
import no.novari.flyt.archive.gateway.dispatch.model.instance.JournalpostDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class RecordsDispatchService(
    private val recordDispatchService: RecordDispatchService,
    private val dispatchMessageFormattingService: DispatchMessageFormattingService,
) {
    fun dispatch(
        caseId: String,
        journalpostDtos: List<JournalpostDto>,
    ): Mono<RecordsDispatchResult> {
        log.info("Dispatching records")
        if (journalpostDtos.isEmpty()) {
            return Mono.just(RecordsDispatchResult.accepted(emptyList()))
        }

        return Flux
            .fromIterable(journalpostDtos)
            .concatMap { journalpostDto -> recordDispatchService.dispatch(caseId, journalpostDto) }
            .takeUntil { recordDispatchResult -> recordDispatchResult.status != DispatchStatus.ACCEPTED }
            .collectList()
            .map { recordDispatchResults ->
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
            }.doOnError { error -> log.error("Journalposts dispatch failed", error) }
            .onErrorResume {
                Mono.just(
                    RecordsDispatchResult.failed(
                        "Journalposts dispatch failed",
                        "possible journalposts with unknown ids",
                    ),
                )
            }.doOnNext { result -> log.info("Dispatch result={}", result.toString()) }
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
