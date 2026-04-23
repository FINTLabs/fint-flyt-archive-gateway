package no.novari.flyt.archive.gateway.dispatch.model.instance

import jakarta.validation.constraints.NotNull
import java.util.Optional
import kotlin.jvm.JvmName

data class AdresseDto(
    @get:JvmName("getAdresselinjeOrNull")
    val adresselinje: Collection<@NotNull String>? = null,
    @get:JvmName("getPostnummerOrNull")
    val postnummer: String? = null,
    @get:JvmName("getPoststedOrNull")
    val poststed: String? = null,
) {
    fun getAdresselinje(): Optional<Collection<String>> = Optional.ofNullable(adresselinje)

    fun getPostnummer(): Optional<String> = Optional.ofNullable(postnummer)

    fun getPoststed(): Optional<String> = Optional.ofNullable(poststed)

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
