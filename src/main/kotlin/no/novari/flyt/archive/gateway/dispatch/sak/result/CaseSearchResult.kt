package no.novari.flyt.archive.gateway.dispatch.sak.result

import no.novari.flyt.archive.gateway.dispatch.DispatchStatus
import kotlin.ConsistentCopyVisibility

@ConsistentCopyVisibility
data class CaseSearchResult private constructor(
    val status: DispatchStatus,
    val archiveCaseIds: List<String>? = null,
    val errorMessage: String? = null,
) {
    companion object {
        @JvmStatic
        fun accepted(archiveCaseIds: List<String>?) =
            CaseSearchResult(DispatchStatus.ACCEPTED, archiveCaseIds = archiveCaseIds ?: emptyList())

        @JvmStatic
        fun declined(errorMessage: String) = CaseSearchResult(DispatchStatus.DECLINED, errorMessage = errorMessage)

        @JvmStatic
        fun failed() = CaseSearchResult(DispatchStatus.FAILED)

        @JvmStatic
        fun timedOut() =
            CaseSearchResult(
                DispatchStatus.FAILED,
                errorMessage = "Case search timed out. No response from destination.",
            )
    }
}
