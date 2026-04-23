package no.novari.flyt.archive.gateway.dispatch

import kotlin.ConsistentCopyVisibility

@ConsistentCopyVisibility
data class DispatchResult private constructor(
    val status: DispatchStatus,
    val archiveCaseAndRecordsIds: String? = null,
    val errorMessage: String? = null,
) {
    companion object {
        @JvmStatic
        fun accepted(archiveCaseId: String) = DispatchResult(DispatchStatus.ACCEPTED, archiveCaseId)

        @JvmStatic
        fun declined(errorMessage: String) = DispatchResult(DispatchStatus.DECLINED, errorMessage = errorMessage)

        @JvmStatic
        fun failed() = failed(null)

        @JvmStatic
        fun failed(errorMessage: String?) = DispatchResult(DispatchStatus.FAILED, errorMessage = errorMessage)
    }
}
