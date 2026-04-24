package no.novari.flyt.archive.gateway.template.model

import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ValuePredicate(
    @field:NotBlank
    @field:Pattern(regexp = "[^.]*")
    val key: String? = null,
    val defined: Boolean? = null,
    val value: String? = null,
    val notValue: String? = null,
) {
    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder {
        private var key: String? = null
        private var defined: Boolean? = null
        private var value: String? = null
        private var notValue: String? = null

        fun key(key: String?) = apply { this.key = key }

        fun defined(defined: Boolean?) = apply { this.defined = defined }

        fun value(value: String?) = apply { this.value = value }

        fun notValue(notValue: String?) = apply { this.notValue = notValue }

        fun build() = ValuePredicate(key = key, defined = defined, value = value, notValue = notValue)
    }
}
