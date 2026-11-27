package no.novari.flyt.archive.gateway.dispatch.model.instance

data class CaseSearchParametersDto(
    val arkivdel: Boolean = false,
    val administrativEnhet: Boolean = false,
    val tilgangsrestriksjon: Boolean = false,
    val saksmappetype: Boolean = false,
    val saksstatus: Boolean = false,
    val tittel: Boolean = false,
    val klassering: Boolean = false,
    val klasseringRekkefolge: String? = null,
    val klasseringKlassifikasjonssystem: Boolean? = null,
    val klasseringKlasseId: Boolean? = null,
) {
    fun getKlasseringKlassifikasjonssystem(): Boolean = klasseringKlassifikasjonssystem ?: false

    fun getKlasseringKlasseId(): Boolean = klasseringKlasseId ?: false
}
