package no.novari.flyt.archive.gateway.dispatch.model.instance

data class AvskrivningDto(
    val avskrivningsdato: String? = null,
    val avskrivningsmate: String? = null,
) {
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
