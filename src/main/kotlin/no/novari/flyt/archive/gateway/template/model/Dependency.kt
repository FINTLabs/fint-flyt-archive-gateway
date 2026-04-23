package no.novari.flyt.archive.gateway.template.model

import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.validation.Valid

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Dependency(
    val hasAnyCombination: Collection<Collection<@Valid ValuePredicate>>? = null,
) {
    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder {
        private var hasAnyCombination: Collection<Collection<ValuePredicate>>? = null

        fun hasAnyCombination(hasAnyCombination: Collection<Collection<ValuePredicate>>?) =
            apply {
                this.hasAnyCombination = hasAnyCombination
            }

        fun build() = Dependency(hasAnyCombination = hasAnyCombination)
    }
}
