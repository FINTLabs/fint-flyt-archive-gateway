package no.novari.flyt.archive.gateway.dispatch

import jakarta.validation.Valid
import no.novari.flyt.archive.gateway.dispatch.model.CaseDispatchType
import no.novari.flyt.archive.gateway.dispatch.model.instance.ArchiveInstance
import no.novari.flyt.archive.gateway.dispatch.model.instance.JournalpostDto
import no.novari.flyt.archive.gateway.dispatch.sak.CaseDispatchService
import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeaders
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class DispatchService(
    private val caseDispatchService: CaseDispatchService,
    private val recordsProcessingService: RecordsProcessingService,
) {
    fun process(
        instanceFlowHeaders: InstanceFlowHeaders,
        @Valid archiveInstance: ArchiveInstance,
    ): Mono<DispatchResult> {
        log.info("Dispatching instance with headers={}", instanceFlowHeaders)

        return when (archiveInstance.type) {
            CaseDispatchType.NEW -> {
                processNew(archiveInstance)
            }

            CaseDispatchType.BY_ID -> {
                processById(archiveInstance)
            }

            CaseDispatchType.BY_SEARCH_OR_NEW -> {
                processBySearchOrNew(archiveInstance)
            }

            null -> {
                Mono.just(DispatchResult.failed("Missing dispatch type"))
            }
        }.doOnNext { dispatchResult -> logDispatchResult(instanceFlowHeaders, dispatchResult) }
            .doOnError { error ->
                log.error("Failed to dispatch instance with headers={}", instanceFlowHeaders, error)
            }
    }

    private fun logDispatchResult(
        instanceFlowHeaders: InstanceFlowHeaders,
        dispatchResult: DispatchResult,
    ) {
        when (dispatchResult.status) {
            DispatchStatus.ACCEPTED -> {
                log.info("Successfully dispatched instance with headers={}", instanceFlowHeaders)
            }

            DispatchStatus.DECLINED -> {
                log.info(
                    "Dispatch was declined for instance with headers={}",
                    instanceFlowHeaders,
                )
            }

            DispatchStatus.FAILED -> {
                log.error("Failed to dispatch instance with headers={}", instanceFlowHeaders)
            }
        }
    }

    private fun processNew(archiveInstance: ArchiveInstance): Mono<DispatchResult> {
        val newCase = archiveInstance.newCase ?: return Mono.just(DispatchResult.failed("Missing new case"))

        return caseDispatchService.dispatch(newCase).flatMap { caseDispatchResult ->
            when (caseDispatchResult.status) {
                DispatchStatus.ACCEPTED -> {
                    newCase.journalpost
                        ?.let { journalpostDtos ->
                            recordsProcessingService.processRecords(
                                caseDispatchResult.archiveCaseId!!,
                                true,
                                journalpostDtos,
                            )
                        }
                        ?: Mono.just(DispatchResult.accepted(caseDispatchResult.archiveCaseId!!))
                }

                DispatchStatus.DECLINED -> {
                    Mono.just(
                        DispatchResult.declined(
                            "Sak was declined by the destination with message='${caseDispatchResult.errorMessage}'",
                        ),
                    )
                }

                DispatchStatus.FAILED -> {
                    Mono.just(DispatchResult.failed("Sak dispatch failed"))
                }
            }
        }
    }

    private fun processById(archiveInstance: ArchiveInstance): Mono<DispatchResult> =
        recordsProcessingService.processRecords(
            archiveInstance.caseId.orEmpty(),
            false,
            archiveInstance.journalpost.orEmpty(),
        )

    private fun processBySearchOrNew(archiveInstance: ArchiveInstance): Mono<DispatchResult> {
        val newCase = archiveInstance.newCase ?: return Mono.just(DispatchResult.failed("Missing new case"))
        val journalpostDtos: List<JournalpostDto>? = newCase.journalpost

        return caseDispatchService.findCasesBySearch(archiveInstance).flatMap { caseSearchResult ->
            when (caseSearchResult.status) {
                DispatchStatus.ACCEPTED -> {
                    val archiveCaseIds = caseSearchResult.archiveCaseIds.orEmpty()

                    when {
                        archiveCaseIds.size > 1 -> {
                            Mono.just(
                                DispatchResult.declined("Found multiple cases: ${archiveCaseIds.joinToString(", ")}"),
                            )
                        }

                        archiveCaseIds.isEmpty() -> {
                            log.info("Found no cases")
                            processNew(archiveInstance)
                        }

                        else -> {
                            val archiveCaseId = archiveCaseIds.first()
                            log.info("Found case with id='{}'", archiveCaseId)

                            journalpostDtos
                                ?.takeIf { it.isNotEmpty() }
                                ?.let {
                                    recordsProcessingService.processRecords(
                                        archiveCaseId,
                                        false,
                                        it,
                                    )
                                }
                                ?: Mono.just(DispatchResult.accepted(archiveCaseId))
                        }
                    }
                }

                DispatchStatus.DECLINED -> {
                    Mono.just(DispatchResult.declined(caseSearchResult.errorMessage.orEmpty()))
                }

                DispatchStatus.FAILED -> {
                    Mono.just(DispatchResult.failed())
                }
            }
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(DispatchService::class.java)
    }
}
