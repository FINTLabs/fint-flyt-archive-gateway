package no.novari.flyt.archive.gateway.dispatch.model.instance

import java.util.Optional
import kotlin.jvm.JvmName

data class KontaktinformasjonDto(
    @get:JvmName("getEpostadresseOrNull")
    val epostadresse: String? = null,
    @get:JvmName("getMobiltelefonnummerOrNull")
    val mobiltelefonnummer: String? = null,
    @get:JvmName("getTelefonnummerOrNull")
    val telefonnummer: String? = null,
) {
    fun getEpostadresse(): Optional<String> = Optional.ofNullable(epostadresse)

    fun getTelefonnummer(): Optional<String> = Optional.ofNullable(telefonnummer)

    fun getMobiltelefonnummer(): Optional<String> = Optional.ofNullable(mobiltelefonnummer)

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
