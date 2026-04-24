package no.novari.flyt.archive.gateway.dispatch.file.result

import no.novari.fint.model.resource.Link
import no.novari.flyt.archive.gateway.dispatch.DispatchStatus
import java.util.UUID
import kotlin.ConsistentCopyVisibility

@ConsistentCopyVisibility
data class FileDispatchResult private constructor(
    val status: DispatchStatus,
    val fileId: UUID? = null,
    val archiveFileLink: Link? = null,
    val errorMessage: String? = null,
) {
    companion object {
        @JvmStatic
        fun accepted(
            fileId: UUID,
            archiveFileLink: Link,
        ) = FileDispatchResult(DispatchStatus.ACCEPTED, fileId = fileId, archiveFileLink = archiveFileLink)

        @JvmStatic
        fun declined(
            fileId: UUID,
            errorMessage: String,
        ) = FileDispatchResult(DispatchStatus.DECLINED, fileId = fileId, errorMessage = errorMessage)

        @JvmStatic
        fun couldNotBeRetrieved(fileId: UUID) =
            FileDispatchResult(DispatchStatus.FAILED, fileId = fileId, errorMessage = "Could not retrieve file")

        @JvmStatic
        fun noFileId() = FileDispatchResult(DispatchStatus.FAILED, errorMessage = "No fileId")

        @JvmStatic
        fun failed(fileId: UUID?) = FileDispatchResult(DispatchStatus.FAILED, fileId = fileId)

        @JvmStatic
        fun timedOut(fileId: UUID) =
            FileDispatchResult(
                DispatchStatus.FAILED,
                fileId = fileId,
                errorMessage = "File dispatch timed out. No response from destination.",
            )
    }
}
