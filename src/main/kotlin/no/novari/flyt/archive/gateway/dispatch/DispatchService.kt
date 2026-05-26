package no.novari.flyt.archive.gateway.dispatch

import jakarta.validation.Valid
import no.novari.flyt.archive.gateway.dispatch.model.CaseDispatchType
import no.novari.flyt.archive.gateway.dispatch.model.instance.ArchiveInstance
import no.novari.flyt.archive.gateway.dispatch.model.instance.JournalpostDto
import no.novari.flyt.archive.gateway.dispatch.sak.CaseDispatchService
import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeaders
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class DispatchService(
    private val caseDispatchService: CaseDispatchService,
    private val recordsProcessingService: RecordsProcessingService,
) {
    fun process(
        instanceFlowHeaders: InstanceFlowHeaders,
        @Valid archiveInstance: ArchiveInstance,
    ): DispatchResult {
        log.info("Dispatching instance with headers={}", instanceFlowHeaders)

        val dispatchResult =
            try {
                when (archiveInstance.type) {
                    CaseDispatchType.NEW -> processNew(archiveInstance)
                    CaseDispatchType.BY_ID -> processById(archiveInstance)
                    CaseDispatchType.BY_SEARCH_OR_NEW -> processBySearchOrNew(archiveInstance)
                    null -> DispatchResult.failed("Missing dispatch type")
                }
            } catch (error: Throwable) {
                log.error("Failed to dispatch instance with headers={}", instanceFlowHeaders, error)
                throw error
            }

        logDispatchResult(instanceFlowHeaders, dispatchResult)
        return dispatchResult
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

    private fun processNew(archiveInstance: ArchiveInstance): DispatchResult {
        val newCase = archiveInstance.newCase ?: return DispatchResult.failed("Missing new case")

        val caseDispatchResult = caseDispatchService.dispatch(newCase)
        return when (caseDispatchResult.status) {
            DispatchStatus.ACCEPTED -> {
                val journalpostDtos = newCase.journalpost
                if (journalpostDtos != null) {
                    recordsProcessingService.processRecords(
                        caseDispatchResult.archiveCaseId!!,
                        true,
                        journalpostDtos,
                    )
                } else {
                    DispatchResult.accepted(caseDispatchResult.archiveCaseId!!)
                }
            }

            DispatchStatus.DECLINED -> {
                DispatchResult.declined(
                    "Sak was declined by the destination with message='${caseDispatchResult.errorMessage}'",
                )
            }

            DispatchStatus.FAILED -> {
                DispatchResult.failed("Sak dispatch failed")
            }
        }
    }

    private fun processById(archiveInstance: ArchiveInstance): DispatchResult =
        recordsProcessingService.processRecords(
            archiveInstance.caseId.orEmpty(),
            false,
            archiveInstance.journalpost.orEmpty(),
        )

    private fun processBySearchOrNew(archiveInstance: ArchiveInstance): DispatchResult {
        val newCase = archiveInstance.newCase ?: return DispatchResult.failed("Missing new case")
        val journalpostDtos: List<JournalpostDto>? = newCase.journalpost

        val caseSearchResult = caseDispatchService.findCasesBySearch(archiveInstance)
        return when (caseSearchResult.status) {
            DispatchStatus.ACCEPTED -> {
                val archiveCaseIds = caseSearchResult.archiveCaseIds.orEmpty()

                when {
                    archiveCaseIds.size > 1 -> {
                        DispatchResult.declined("Found multiple cases: ${archiveCaseIds.joinToString(", ")}")
                    }

                    archiveCaseIds.isEmpty() -> {
                        log.info("Found no cases")
                        processNew(archiveInstance)
                    }

                    else -> {
                        val archiveCaseId = archiveCaseIds.first()
                        log.info("Found case with id='{}'", archiveCaseId)

                        if (!journalpostDtos.isNullOrEmpty()) {
                            recordsProcessingService.processRecords(archiveCaseId, false, journalpostDtos)
                        } else {
                            DispatchResult.accepted(archiveCaseId)
                        }
                    }
                }
            }

            DispatchStatus.DECLINED -> {
                DispatchResult.declined(caseSearchResult.errorMessage.orEmpty())
            }

            DispatchStatus.FAILED -> {
                DispatchResult.failed()
            }
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(DispatchService::class.java)
    }
}
