package no.novari.flyt.archive.gateway.dispatch.model.instance

import java.util.Optional
import kotlin.jvm.JvmName

data class AvskrivningDto(
    @get:JvmName("getAvskrivningsdatoOrNull")
    val avskrivningsdato: String? = null,
    @get:JvmName("getAvskrivningsmateOrNull")
    val avskrivningsmate: String? = null,
) {
    fun getAvskrivningsdato(): Optional<String> = Optional.ofNullable(avskrivningsdato)

    fun getAvskrivningsmate(): Optional<String> = Optional.ofNullable(avskrivningsmate)

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder {
        private var avskrivningsdato: String? = null
        private var avskrivningsmate: String? = null

        fun avskrivningsdato(avskrivningsdato: String?) = apply { this.avskrivningsdato = avskrivningsdato }

        fun avskrivningsmate(avskrivningsmate: String?) = apply { this.avskrivningsmate = avskrivningsmate }

        fun build() = AvskrivningDto(avskrivningsdato = avskrivningsdato, avskrivningsmate = avskrivningsmate)
    }
}
