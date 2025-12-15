package no.novari.flyt.archive.gateway.dispatch.journalpost;

import lombok.extern.slf4j.Slf4j;
import no.novari.flyt.archive.gateway.dispatch.DispatchMessageFormattingService;
import no.novari.flyt.archive.gateway.dispatch.DispatchStatus;
import no.novari.flyt.archive.gateway.dispatch.journalpost.result.RecordDispatchResult;
import no.novari.flyt.archive.gateway.dispatch.journalpost.result.RecordsDispatchResult;
import no.novari.flyt.archive.gateway.dispatch.model.instance.JournalpostDto;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class RecordsDispatchService {

    private final RecordDispatchService recordDispatchService;

    private final DispatchMessageFormattingService dispatchMessageFormattingService;

    public RecordsDispatchService(
            RecordDispatchService recordDispatchService,
            DispatchMessageFormattingService dispatchMessageFormattingService
    ) {
        this.recordDispatchService = recordDispatchService;
        this.dispatchMessageFormattingService = dispatchMessageFormattingService;
    }

    public Mono<RecordsDispatchResult> dispatch(String caseId, List<JournalpostDto> journalpostDtos) {
        log.info("Dispatching records");
        if (journalpostDtos.isEmpty()) {
            return Mono.just(RecordsDispatchResult.accepted(Collections.emptyList()));
        }
        return Flux.fromIterable(journalpostDtos)
                .concatMap(journalpostDto -> recordDispatchService.dispatch(caseId, journalpostDto))
                .takeUntil(recordDispatchResult -> recordDispatchResult.getStatus() != DispatchStatus.ACCEPTED)
                .collectList()
                .map(recordDispatchResults -> {
                            RecordDispatchResult lastResult = recordDispatchResults.getLast();
                            DispatchStatus lastStatus = lastResult.getStatus();

                            List<Long> idsOfsuccessfullyDispatchedRecords = (
                                    lastStatus == DispatchStatus.ACCEPTED
                                            ? recordDispatchResults
                                            : recordDispatchResults.subList(0, recordDispatchResults.size() - 1)
                            ).stream().map(RecordDispatchResult::getJournalpostId).toList();

                            return switch (lastStatus) {
                                case ACCEPTED -> RecordsDispatchResult.accepted(idsOfsuccessfullyDispatchedRecords);
                                case DECLINED -> RecordsDispatchResult.declined(
                                        lastResult.getErrorMessage(),
                                        createFunctionalWarningMessages(idsOfsuccessfullyDispatchedRecords).orElse(null)
                                );
                                case FAILED -> RecordsDispatchResult.failed(
                                        lastResult.getErrorMessage(),
                                        createFunctionalWarningMessages(idsOfsuccessfullyDispatchedRecords).orElse(null)
                                );
                            };
                        }
                )
                .doOnError(e -> log.error("Journalposts dispatch failed", e))
                .onErrorResume(e -> Mono.just(
                                RecordsDispatchResult.failed(
                                        "Journalposts dispatch failed",
                                        "possible journalposts with unknown ids"
                                )
                        )
                ).doOnNext(result -> log.info("Dispatch result={}", result.toString()));

    }

    private Optional<String> createFunctionalWarningMessages(List<Long> idsOfsuccessfullyDispatchedRecords) {
        return dispatchMessageFormattingService.createFunctionalWarningMessage(
                "journalpost",
                "id",
                idsOfsuccessfullyDispatchedRecords.stream().map(String::valueOf).toList()
        );
    }

}
