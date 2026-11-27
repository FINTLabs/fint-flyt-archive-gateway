package no.novari.flyt.archive.gateway.dispatch.model

data class File(
    val name: String? = null,
    val type: String? = null,
    val encoding: String? = null,
    val contents: ByteArray? = null,
)
