package no.novari.flyt.archive.gateway.dispatch.mapping

import no.novari.fint.model.resource.Link
import no.novari.fint.model.resource.arkiv.noark.PartResource
import no.novari.flyt.archive.gateway.dispatch.model.instance.PartDto
import org.springframework.stereotype.Service

@Service
class PartMappingService(
    private val kontaktinformasjonMappingService: KontaktinformasjonMappingService,
    private val adresseMappingService: AdresseMappingService,
) {
    fun toPartResource(partDto: PartDto?): PartResource? =
        partDto?.let {
            PartResource().apply {
                partDto.partNavn?.let(::setPartNavn)
                partDto.partRolle?.let(Link::with)?.let(::addPartRolle)
                partDto.kontaktperson?.let(::setKontaktperson)
                partDto.organisasjonsnummer?.let(::setOrganisasjonsnummer)
                partDto.fodselsnummer?.let(::setFodselsnummer)
                partDto.adresse?.let(adresseMappingService::toAdresseResource)?.let(::setAdresse)
                partDto.kontaktinformasjon
                    ?.let(kontaktinformasjonMappingService::toKontaktinformasjon)
                    ?.let(::setKontaktinformasjon)
            }
        }
}
