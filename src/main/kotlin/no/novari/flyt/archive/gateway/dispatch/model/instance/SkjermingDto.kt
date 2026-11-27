package no.novari.flyt.archive.gateway.dispatch.model.instance

data class SkjermingDto(
    val tilgangsrestriksjon: String? = null,
    val skjermingshjemmel: String? = null,
) {
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
