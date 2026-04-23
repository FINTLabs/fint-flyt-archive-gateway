package no.novari.flyt.archive.gateway.dispatch.journalpost.result

import no.novari.flyt.archive.gateway.dispatch.DispatchStatus
import kotlin.ConsistentCopyVisibility

@ConsistentCopyVisibility
data class RecordDispatchResult private constructor(
    val status: DispatchStatus,
    val journalpostId: Long? = null,
    val errorMessage: String? = null,
) {
    companion object {
        @JvmStatic
        fun accepted(journalpostId: Long) = RecordDispatchResult(DispatchStatus.ACCEPTED, journalpostId = journalpostId)

        @JvmStatic
        fun declined(errorMessage: String) = RecordDispatchResult(DispatchStatus.DECLINED, errorMessage = errorMessage)

        @JvmStatic
        fun failed(errorMessage: String) = RecordDispatchResult(DispatchStatus.FAILED, errorMessage = errorMessage)

        @JvmStatic
        fun timedOut() =
            RecordDispatchResult(
                DispatchStatus.FAILED,
                errorMessage = "Record dispatch timed out. No response from destination.",
            )
    }
}
