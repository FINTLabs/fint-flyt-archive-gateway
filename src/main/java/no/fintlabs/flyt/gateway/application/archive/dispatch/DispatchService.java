package no.fintlabs.flyt.gateway.application.archive.dispatch;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.flyt.gateway.application.archive.dispatch.model.instance.ArchiveInstance;
import no.fintlabs.flyt.gateway.application.archive.dispatch.model.instance.JournalpostDto;
import no.fintlabs.flyt.gateway.application.archive.dispatch.sak.CaseDispatchService;
import no.fintlabs.flyt.gateway.application.archive.dispatch.sak.result.CaseDispatchResult;
import no.fintlabs.flyt.gateway.application.archive.kafka.error.InstanceDispatchingErrorProducerService;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DispatchService {

    private final CaseDispatchService caseDispatchService;
    private final RecordsProcessingService recordsProcessingService;
    private final InstanceDispatchingErrorProducerService instanceDispatchingErrorProducerService;

    public DispatchService(
            CaseDispatchService caseDispatchService,
            RecordsProcessingService recordsProcessingService,
            InstanceDispatchingErrorProducerService instanceDispatchingErrorProducerService
    ) {
        this.caseDispatchService = caseDispatchService;
        this.recordsProcessingService = recordsProcessingService;
        this.instanceDispatchingErrorProducerService = instanceDispatchingErrorProducerService;
    }

    public Mono<DispatchResult> process(InstanceFlowHeaders instanceFlowHeaders, @Valid ArchiveInstance archiveInstance) {
        log.info("Dispatching instance with headers={}", instanceFlowHeaders);
        return (switch (archiveInstance.getType()) {
            case NEW -> processNew(archiveInstance);
            case BY_ID -> processById(archiveInstance);
            case BY_SEARCH_OR_NEW -> processBySearchOrNew(archiveInstance);
        })
                .doOnNext(dispatchResult -> logDispatchResult(instanceFlowHeaders, dispatchResult))
                .onErrorResume(e -> {
                    if (e instanceof IllegalStateException) {
                        return handleDispatchError(instanceFlowHeaders, e, "IllegalStateException encountered during dispatch", instanceDispatchingErrorProducerService);
                    } else if (e instanceof IllegalArgumentException) {
                        return handleDispatchError(instanceFlowHeaders, e, "IllegalArgumentException encountered during dispatch", instanceDispatchingErrorProducerService);
                    } else if (e instanceof NullPointerException) {
                        return handleDispatchError(instanceFlowHeaders, e, "NullPointerException encountered during dispatch", instanceDispatchingErrorProducerService);
                    } else {
                        return handleDispatchError(instanceFlowHeaders, e, "Unexpected exception encountered during dispatch", instanceDispatchingErrorProducerService);
                    }
                })
                .doOnError(e -> log.error("Failed to dispatch instance with headers={}", instanceFlowHeaders, e))
                .onErrorReturn(RuntimeException.class, DispatchResult.failed());
    }

    private void logDispatchResult(InstanceFlowHeaders instanceFlowHeaders, DispatchResult dispatchResult) {
        if (dispatchResult.getStatus() == DispatchStatus.ACCEPTED) {
            log.info("Successfully dispatched instance with headers={}", instanceFlowHeaders);
        } else if (dispatchResult.getStatus() == DispatchStatus.DECLINED) {
            log.info("Dispatch was declined for instance with headers={}", instanceFlowHeaders);
        } else if (dispatchResult.getStatus() == DispatchStatus.FAILED) {
            log.error("Failed to dispatch instance with headers={}", instanceFlowHeaders);
        }
    }

    private Mono<DispatchResult> processNew(ArchiveInstance archiveInstance) {
        return caseDispatchService.dispatch(archiveInstance.getNewCase())
                .flatMap(caseDispatchResult -> switch (caseDispatchResult.getStatus()) {
                            case ACCEPTED -> archiveInstance.getNewCase().getJournalpost()
                                    .map(journalpostDtos -> recordsProcessingService.processRecords(
                                                    caseDispatchResult.getArchiveCaseId(),
                                                    true,
                                                    journalpostDtos
                                            )
                                    ).orElse(Mono.just(DispatchResult.accepted(caseDispatchResult.getArchiveCaseId())));
                            case DECLINED -> Mono.just(DispatchResult.declined(
                                    "Sak was declined by the destination with message='" + caseDispatchResult.getErrorMessage() + "'"
                            ));
                            case FAILED -> Mono.just(DispatchResult.failed("Sak dispatch failed"));
                        }
                );
    }

    private Mono<DispatchResult> processById(ArchiveInstance archiveInstance) {
        return recordsProcessingService.processRecords(archiveInstance.getCaseId(), false, archiveInstance.getJournalpost());
    }

    @Getter
    @AllArgsConstructor
    private static class CaseInfo {
        private final boolean newCase;
        private final String caseId;
    }

    private Mono<DispatchResult> processBySearchOrNew(ArchiveInstance archiveInstance) {
        Optional<List<JournalpostDto>> journalpostDtosOptional = archiveInstance.getNewCase().getJournalpost();
        return caseDispatchService.findCasesBySearch(archiveInstance)
                .flatMap(cases -> {
                            if (cases.size() > 1) {
                                String caseIds = cases.stream()
                                        .map(sakResource -> sakResource.getMappeId().getIdentifikatorverdi())
                                        .collect(Collectors.joining(", "));

                                return Mono.just(DispatchResult.declined("Found multiple cases: " + caseIds));
                            }

                            return (
                                    cases.size() == 1

                                            ? Mono.just(new CaseInfo(
                                            false,
                                            cases.get(0).getMappeId().getIdentifikatorverdi()))

                                            : caseDispatchService.dispatch(archiveInstance.getNewCase())
                                            .map(CaseDispatchResult::getArchiveCaseId)
                                            .map(caseId -> new CaseInfo(true, caseId))

                            ).flatMap(caseInfo ->
                                    journalpostDtosOptional
                                            .filter(journalpostDtos -> !journalpostDtos.isEmpty())
                                            .map(
                                                    journalpostDtos -> recordsProcessingService.processRecords(
                                                            caseInfo.getCaseId(),
                                                            caseInfo.isNewCase(),
                                                            journalpostDtos
                                                    )
                                            ).orElse(Mono.just(
                                                    DispatchResult.accepted(caseInfo.getCaseId())
                                            ))
                            );
                        }
                );
    }

    private Mono<DispatchResult> handleDispatchError(
            InstanceFlowHeaders instanceFlowHeaders,
            Throwable e,
            String logMessage,
            InstanceDispatchingErrorProducerService instanceDispatchingErrorProducerService
    ) {
        String errorMessage = (e != null && e.getMessage() != null) ? e.getMessage() : "test";

        log.error("{}: {}", logMessage, errorMessage, e);

        instanceDispatchingErrorProducerService.publishGeneralSystemErrorEvent(
                instanceFlowHeaders,
                "An error occurred during dispatch: " + errorMessage
        );

        return Mono.error(Objects.requireNonNullElseGet(e, () -> new IllegalStateException("An unknown error occurred during dispatch")));
    }

}
