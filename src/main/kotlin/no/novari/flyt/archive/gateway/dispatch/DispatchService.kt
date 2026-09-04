package no.novari.flyt.archive.gateway.dispatch

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.Valid
import no.novari.flyt.archive.gateway.dispatch.model.CaseDispatchType
import no.novari.flyt.archive.gateway.dispatch.model.instance.ArchiveInstance
import no.novari.flyt.archive.gateway.dispatch.model.instance.JournalpostDto
import no.novari.flyt.archive.gateway.dispatch.sak.CaseDispatchService
import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeaders
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
        log.atInfo {
            message = "Dispatching instance with headers={}"
            arguments = arrayOf(instanceFlowHeaders)
        }

        val dispatchResult =
            try {
                when (archiveInstance.type) {
                    CaseDispatchType.NEW -> processNew(archiveInstance)
                    CaseDispatchType.BY_ID -> processById(archiveInstance)
                    CaseDispatchType.BY_SEARCH_OR_NEW -> processBySearchOrNew(archiveInstance)
                    null -> DispatchResult.failed("Missing dispatch type")
                }
            } catch (error: Throwable) {
                log.atError {
                    message = "Failed to dispatch instance with headers={}"
                    arguments = arrayOf(instanceFlowHeaders)
                    cause = error
                }
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
                log.atInfo {
                    message = "Successfully dispatched instance with headers={}"
                    arguments = arrayOf(instanceFlowHeaders)
                }
            }

            DispatchStatus.DECLINED -> {
                log.atInfo {
                    message = "Dispatch was declined for instance with headers={}"
                    arguments = arrayOf(instanceFlowHeaders)
                }
            }

            DispatchStatus.FAILED -> {
                log.atError {
                    message = "Failed to dispatch instance with headers={}"
                    arguments = arrayOf(instanceFlowHeaders)
                }
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
                        log.info { "Found no cases" }
                        processNew(archiveInstance)
                    }

                    else -> {
                        val archiveCaseId = archiveCaseIds.first()
                        log.atInfo {
                            message = "Found case with id='{}'"
                            arguments = arrayOf(archiveCaseId)
                        }

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
        private val log = KotlinLogging.logger {}
    }
}
