package no.novari.flyt.archive.gateway.dispatch;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import no.novari.flyt.archive.gateway.dispatch.model.instance.ArchiveInstance;
import no.novari.flyt.archive.gateway.dispatch.model.instance.JournalpostDto;
import no.novari.flyt.archive.gateway.dispatch.sak.CaseDispatchService;
import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class DispatchService {

    private final CaseDispatchService caseDispatchService;
    private final RecordsProcessingService recordsProcessingService;

    public DispatchService(
            CaseDispatchService caseDispatchService,
            RecordsProcessingService recordsProcessingService
    ) {
        this.caseDispatchService = caseDispatchService;
        this.recordsProcessingService = recordsProcessingService;
    }

    public Mono<DispatchResult> process(InstanceFlowHeaders instanceFlowHeaders, @Valid ArchiveInstance archiveInstance) {
        log.info("Dispatching instance with headers={}", instanceFlowHeaders);
        return (switch (archiveInstance.getType()) {
            case NEW -> processNew(archiveInstance);
            case BY_ID -> processById(archiveInstance);
            case BY_SEARCH_OR_NEW -> processBySearchOrNew(archiveInstance);
        })
                .doOnNext(dispatchResult -> logDispatchResult(instanceFlowHeaders, dispatchResult))
                .doOnError(e -> log.error("Failed to dispatch instance with headers={}", instanceFlowHeaders, e));
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

    private Mono<DispatchResult> processBySearchOrNew(ArchiveInstance archiveInstance) {
        Optional<List<JournalpostDto>> journalpostDtosOptional = archiveInstance.getNewCase().getJournalpost();
        return caseDispatchService.findCasesBySearch(archiveInstance)
                .flatMap(caseSearchResult -> switch (caseSearchResult.getStatus()) {
                            case ACCEPTED -> {
                                if (caseSearchResult.getArchiveCaseIds().size() > 1) {
                                    String caseIds = String.join(", ", caseSearchResult.getArchiveCaseIds());
                                    yield Mono.just(DispatchResult.declined("Found multiple cases: " + caseIds));
                                } else {
                                    if (caseSearchResult.getArchiveCaseIds().isEmpty()) {
                                        log.info("Found no cases");
                                        yield processNew(archiveInstance);
                                    } else {
                                        log.info("Found case with id='{}'", caseSearchResult.getArchiveCaseIds().getFirst());
                                        yield journalpostDtosOptional
                                                .filter(journalpostDtos -> !journalpostDtos.isEmpty())
                                                .map(
                                                        journalpostDtos -> recordsProcessingService.processRecords(
                                                                caseSearchResult.getArchiveCaseIds().getFirst(),
                                                                false,
                                                                journalpostDtos
                                                        )
                                                ).orElse(Mono.just(
                                                        DispatchResult.accepted(caseSearchResult.getArchiveCaseIds().getFirst()))
                                                );
                                    }
                                }
                            }
                            case DECLINED -> Mono.just(DispatchResult.declined(caseSearchResult.getErrorMessage()));
                            case FAILED -> Mono.just(DispatchResult.failed());
                        }
                );
    }
}
