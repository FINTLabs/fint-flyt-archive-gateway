package no.novari.flyt.archive.gateway.dispatch.model.instance

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotNull
import java.util.Optional
import java.util.UUID
import kotlin.jvm.JvmName

data class DokumentobjektDto(
    @get:JvmName("getVariantformatOrNull")
    val variantformat: String? = null,
    @get:JvmName("getFilformatOrNull")
    val filformat: String? = null,
    @get:JvmName("getFormatOrNull")
    val format: String? = null,
    @field:JsonProperty("fil")
    @field:NotNull
    @get:JvmName("getFileIdOrNull")
    val fileId: UUID? = null,
) {
    fun getVariantformat(): Optional<String> = Optional.ofNullable(variantformat)

    fun getFilformat(): Optional<String> = Optional.ofNullable(filformat)

    fun getFormat(): Optional<String> = Optional.ofNullable(format)

    fun getFileId(): Optional<UUID> = Optional.ofNullable(fileId)

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder {
        private var variantformat: String? = null
        private var filformat: String? = null
        private var format: String? = null
        private var fileId: UUID? = null

        fun variantformat(variantformat: String?) = apply { this.variantformat = variantformat }

        fun filformat(filformat: String?) = apply { this.filformat = filformat }

        fun format(format: String?) = apply { this.format = format }

        fun fileId(fileId: UUID?) = apply { this.fileId = fileId }

        fun build() =
            DokumentobjektDto(
                variantformat = variantformat,
                filformat = filformat,
                format = format,
                fileId = fileId,
            )
    }
}
