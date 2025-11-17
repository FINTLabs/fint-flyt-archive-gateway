package no.novari.flyt.archive.gateway.dispatch;

import no.novari.flyt.archive.gateway.dispatch.journalpost.RecordsDispatchService;
import no.novari.flyt.archive.gateway.dispatch.model.instance.JournalpostDto;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class RecordsProcessingService {

    private final RecordsDispatchService recordsDispatchService;
    private final DispatchMessageFormattingService dispatchMessageFormattingService;

    public RecordsProcessingService(
            RecordsDispatchService recordsDispatchService,
            DispatchMessageFormattingService dispatchMessageFormattingService
    ) {
        this.recordsDispatchService = recordsDispatchService;
        this.dispatchMessageFormattingService = dispatchMessageFormattingService;
    }

    public Mono<DispatchResult> processRecords(String archiveCaseId, boolean newCase, List<JournalpostDto> journalpostDtos) {
        return recordsDispatchService.dispatch(
                archiveCaseId,
                journalpostDtos
        ).map(recordsDispatchResult ->
                switch (recordsDispatchResult.getStatus()) {
                    case ACCEPTED -> DispatchResult.accepted(
                            dispatchMessageFormattingService.formatCaseIdAndJournalpostIds(
                                    archiveCaseId,
                                    recordsDispatchResult.getJournalpostIds()
                            )
                    );
                    case DECLINED -> DispatchResult.declined(
                            "Journalpost was declined by the destination. " +
                                    dispatchMessageFormattingService.combineFunctionalWarningMessages(
                                            archiveCaseId,
                                            newCase,
                                            recordsDispatchResult.getFunctionalWarningMessage() == null
                                                    ? List.of()
                                                    : List.of(recordsDispatchResult.getFunctionalWarningMessage())
                                    ).map(warningmessage -> " " + warningmessage + " ").orElse("") +
                                    "Error message: '" +
                                    recordsDispatchResult.getErrorMessage() +
                                    "'");
                    case FAILED -> DispatchResult.failed("Journalpost dispatch failed." +
                            dispatchMessageFormattingService.combineFunctionalWarningMessages(
                                    archiveCaseId,
                                    newCase,
                                    recordsDispatchResult.getFunctionalWarningMessage() == null
                                            ? List.of()
                                            : List.of(recordsDispatchResult.getFunctionalWarningMessage())
                            ).map(warningmessage -> " " + warningmessage).orElse("")
                    );
                }
        );
    }

}
