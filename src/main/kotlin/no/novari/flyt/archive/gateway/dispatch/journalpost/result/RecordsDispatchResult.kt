package no.novari.flyt.archive.gateway.dispatch.journalpost.result

import no.novari.flyt.archive.gateway.dispatch.DispatchStatus
import kotlin.ConsistentCopyVisibility

@ConsistentCopyVisibility
data class RecordsDispatchResult private constructor(
    val status: DispatchStatus,
    val journalpostIds: List<Long>? = null,
    val errorMessage: String? = null,
    val functionalWarningMessage: String? = null,
) {
    companion object {
        @JvmStatic
        fun accepted(journalpostIds: List<Long>) =
            RecordsDispatchResult(DispatchStatus.ACCEPTED, journalpostIds = journalpostIds)

        @JvmStatic
        fun declined(
            errorMessage: String,
            functionalWarningMessage: String?,
        ) = RecordsDispatchResult(
            DispatchStatus.DECLINED,
            errorMessage = errorMessage,
            functionalWarningMessage = functionalWarningMessage,
        )

        @JvmStatic
        fun failed(
            errorMessage: String,
            functionalWarningMessage: String?,
        ) = RecordsDispatchResult(
            DispatchStatus.FAILED,
            errorMessage = errorMessage,
            functionalWarningMessage = functionalWarningMessage,
        )
    }
}
