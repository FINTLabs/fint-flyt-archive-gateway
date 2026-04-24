package no.novari.flyt.archive.gateway.dispatch.model

data class File(
    val name: String? = null,
    val type: String? = null,
    val encoding: String? = null,
    val contents: ByteArray? = null,
) {
    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder {
        private var name: String? = null
        private var type: String? = null
        private var encoding: String? = null
        private var contents: ByteArray? = null

        fun name(name: String?) = apply { this.name = name }

        fun type(type: String?) = apply { this.type = type }

        fun encoding(encoding: String?) = apply { this.encoding = encoding }

        fun contents(contents: ByteArray?) = apply { this.contents = contents }

        fun build() = File(name = name, type = type, encoding = encoding, contents = contents)
    }
}
