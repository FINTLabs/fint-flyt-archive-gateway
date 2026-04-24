package no.novari.flyt.archive.gateway.dispatch.file.result

import no.novari.fint.model.resource.Link
import no.novari.flyt.archive.gateway.dispatch.DispatchStatus
import java.util.UUID
import kotlin.ConsistentCopyVisibility

@ConsistentCopyVisibility
data class FilesDispatchResult private constructor(
    val status: DispatchStatus,
    val archiveFileLinkPerFileId: Map<UUID, Link>? = null,
    val errorMessage: String? = null,
) {
    companion object {
        @JvmStatic
        fun accepted(archiveFileLinkPerFileId: Map<UUID, Link>) =
            FilesDispatchResult(DispatchStatus.ACCEPTED, archiveFileLinkPerFileId = archiveFileLinkPerFileId)

        @JvmStatic
        fun declined(errorMessage: String) = FilesDispatchResult(DispatchStatus.DECLINED, errorMessage = errorMessage)

        @JvmStatic
        fun failed() = FilesDispatchResult(DispatchStatus.FAILED)
    }
}
