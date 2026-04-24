package no.novari.flyt.archive.gateway.dispatch.mapping

import no.novari.fint.model.resource.Link
import no.novari.fint.model.resource.arkiv.noark.SkjermingResource
import no.novari.flyt.archive.gateway.dispatch.model.instance.SkjermingDto
import org.springframework.stereotype.Service

@Service
class SkjermingMappingService {
    fun toSkjermingResource(skjermingDto: SkjermingDto?): SkjermingResource? =
        skjermingDto?.let {
            SkjermingResource().apply {
                skjermingDto.tilgangsrestriksjon?.let(Link::with)?.let(::addTilgangsrestriksjon)
                skjermingDto.skjermingshjemmel?.let(Link::with)?.let(::addSkjermingshjemmel)
            }
        }
}
