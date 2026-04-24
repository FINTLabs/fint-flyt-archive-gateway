package no.novari.flyt.archive.gateway.dispatch.model.instance

import java.util.Optional
import kotlin.jvm.JvmName

data class SkjermingDto(
    @get:JvmName("getTilgangsrestriksjonOrNull")
    val tilgangsrestriksjon: String? = null,
    @get:JvmName("getSkjermingshjemmelOrNull")
    val skjermingshjemmel: String? = null,
) {
    fun getTilgangsrestriksjon(): Optional<String> = Optional.ofNullable(tilgangsrestriksjon)

    fun getSkjermingshjemmel(): Optional<String> = Optional.ofNullable(skjermingshjemmel)

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder {
        private var tilgangsrestriksjon: String? = null
        private var skjermingshjemmel: String? = null

        fun tilgangsrestriksjon(tilgangsrestriksjon: String?) =
            apply {
                this.tilgangsrestriksjon = tilgangsrestriksjon
            }

        fun skjermingshjemmel(skjermingshjemmel: String?) =
            apply {
                this.skjermingshjemmel = skjermingshjemmel
            }

        fun build() =
            SkjermingDto(
                tilgangsrestriksjon = tilgangsrestriksjon,
                skjermingshjemmel = skjermingshjemmel,
            )
    }
}
