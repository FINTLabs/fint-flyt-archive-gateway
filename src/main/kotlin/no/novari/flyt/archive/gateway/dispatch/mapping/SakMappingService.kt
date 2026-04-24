package no.novari.flyt.archive.gateway.dispatch.mapping

import no.novari.fint.model.resource.Link
import no.novari.fint.model.resource.arkiv.noark.SakResource
import no.novari.flyt.archive.gateway.dispatch.model.instance.SakDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class SakMappingService(
    private val skjermingMappingService: SkjermingMappingService,
    private val klasseMappingService: KlasseMappingService,
    private val partMappingService: PartMappingService,
) {
    fun toSakResource(sakDto: SakDto?): SakResource {
        requireNotNull(sakDto) { "sakDto cannot be null" }

        log.info("Mapping SakDto to SakResource")

        return SakResource()
            .apply {
                sakDto.tittel?.let(::setTittel)
                sakDto.offentligTittel?.let(::setOffentligTittel)
                sakDto.saksmappetype?.let(Link::with)?.let(::addSaksmappetype)
                sakDto.saksstatus?.let(Link::with)?.let(::addSaksstatus)
                sakDto.tilgangsgruppe?.let(Link::with)?.let(::addTilgangsgruppe)
                sakDto.journalenhet?.let(Link::with)?.let(::addJournalenhet)
                sakDto.administrativEnhet?.let(Link::with)?.let(::addAdministrativEnhet)
                sakDto.saksansvarlig?.let(Link::with)?.let(::addSaksansvarlig)
                sakDto.arkivdel?.let(Link::with)?.let(::addArkivdel)
                sakDto.part?.mapNotNull(partMappingService::toPartResource)?.let(::setPart)
                sakDto.skjerming?.let(skjermingMappingService::toSkjermingResource)?.let(::setSkjerming)
                sakDto.klasse?.let(klasseMappingService::toKlasse)?.let(::setKlasse)
            }.also {
                log.info("Successfully mapped SakDto to SakResource")
            }
    }

    companion object {
        private val log = LoggerFactory.getLogger(SakMappingService::class.java)
    }
}
