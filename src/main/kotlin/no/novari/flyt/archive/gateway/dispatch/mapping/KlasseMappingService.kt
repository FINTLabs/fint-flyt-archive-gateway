package no.novari.flyt.archive.gateway.dispatch.mapping

import no.novari.fint.model.resource.Link
import no.novari.fint.model.resource.arkiv.noark.KlasseResource
import no.novari.flyt.archive.gateway.dispatch.model.instance.KlasseDto
import org.springframework.stereotype.Service

@Service
class KlasseMappingService(
    private val skjermingMappingService: SkjermingMappingService,
) {
    fun toKlasse(klasseDtos: List<KlasseDto>?): List<KlasseResource>? = klasseDtos?.map(this::toKlasse)

    private fun toKlasse(klasseDto: KlasseDto): KlasseResource =
        KlasseResource().apply {
            klasseDto.klasseId?.let(::setKlasseId)
            klasseDto.skjerming?.let(skjermingMappingService::toSkjermingResource)?.let(::setSkjerming)
            klasseDto.tittel?.let(::setTittel)
            klasseDto.klassifikasjonssystem?.let(Link::with)?.let(::addKlassifikasjonssystem)
            klasseDto.rekkefolge?.let(::setRekkefolge)
        }
}
