package no.novari.flyt.archive.gateway.resource.sak

data class CaseTitle(
    val value: String,
) {
    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder {
        private var value: String? = null

        fun value(value: String) = apply { this.value = value }

        fun build() = CaseTitle(requireNotNull(value) { "value is required" })
    }
}
