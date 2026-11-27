package no.novari.flyt.archive.gateway.dispatch.model.instance

data class KontaktinformasjonDto(
    val epostadresse: String? = null,
    val mobiltelefonnummer: String? = null,
    val telefonnummer: String? = null,
) {
    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder {
        private var epostadresse: String? = null
        private var mobiltelefonnummer: String? = null
        private var telefonnummer: String? = null

        fun epostadresse(epostadresse: String?) = apply { this.epostadresse = epostadresse }

        fun mobiltelefonnummer(mobiltelefonnummer: String?) =
            apply {
                this.mobiltelefonnummer = mobiltelefonnummer
            }

        fun telefonnummer(telefonnummer: String?) = apply { this.telefonnummer = telefonnummer }

        fun build() =
            KontaktinformasjonDto(
                epostadresse = epostadresse,
                mobiltelefonnummer = mobiltelefonnummer,
                telefonnummer = telefonnummer,
            )
    }
}
