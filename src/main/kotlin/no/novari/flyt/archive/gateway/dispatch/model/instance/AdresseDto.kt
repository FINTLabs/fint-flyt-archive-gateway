package no.novari.flyt.archive.gateway.dispatch.model.instance

import jakarta.validation.constraints.NotNull

data class AdresseDto(
    val adresselinje: Collection<@NotNull String>? = null,
    val postnummer: String? = null,
    val poststed: String? = null,
) {
    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder {
        private var adresselinje: Collection<String>? = null
        private var postnummer: String? = null
        private var poststed: String? = null

        fun adresselinje(adresselinje: Collection<String>?) = apply { this.adresselinje = adresselinje }

        fun postnummer(postnummer: String?) = apply { this.postnummer = postnummer }

        fun poststed(poststed: String?) = apply { this.poststed = poststed }

        fun build() = AdresseDto(adresselinje = adresselinje, postnummer = postnummer, poststed = poststed)
    }
}
