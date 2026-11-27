package no.novari.flyt.archive.gateway.dispatch.model.instance

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotNull
import java.util.UUID

data class DokumentobjektDto(
    val variantformat: String? = null,
    val filformat: String? = null,
    val format: String? = null,
    @field:JsonProperty("fil")
    @field:NotNull
    val fileId: UUID? = null,
) {
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
