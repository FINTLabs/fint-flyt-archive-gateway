package no.novari.flyt.archive.gateway.dispatch.sak.result

import no.novari.flyt.archive.gateway.dispatch.DispatchStatus
import kotlin.ConsistentCopyVisibility

@ConsistentCopyVisibility
data class CaseDispatchResult private constructor(
    val status: DispatchStatus,
    val archiveCaseId: String? = null,
    val errorMessage: String? = null,
) {
    companion object {
        @JvmStatic
        fun accepted(archiveCaseId: String) = CaseDispatchResult(DispatchStatus.ACCEPTED, archiveCaseId = archiveCaseId)

        @JvmStatic
        fun declined(errorMessage: String) = CaseDispatchResult(DispatchStatus.DECLINED, errorMessage = errorMessage)

        @JvmStatic
        fun failed() = CaseDispatchResult(DispatchStatus.FAILED)

        @JvmStatic
        fun timedOut() =
            CaseDispatchResult(
                DispatchStatus.FAILED,
                errorMessage = "Case dispatch timed out. No response from destination.",
            )
    }
}
