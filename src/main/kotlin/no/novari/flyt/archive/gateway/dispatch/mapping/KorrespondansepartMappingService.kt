package no.novari.flyt.archive.gateway.dispatch.mapping

import no.novari.fint.model.resource.Link
import no.novari.fint.model.resource.arkiv.noark.KorrespondansepartResource
import no.novari.flyt.archive.gateway.dispatch.model.instance.KorrespondansepartDto
import org.springframework.stereotype.Service

@Service
class KorrespondansepartMappingService(
    private val adresseMappingService: AdresseMappingService,
    private val kontaktinformasjonMappingService: KontaktinformasjonMappingService,
    private val skjermingMappingService: SkjermingMappingService,
) {
    fun toKorrespondansepartResource(
        korrespondansepartDto: Collection<KorrespondansepartDto>,
    ): List<KorrespondansepartResource> = korrespondansepartDto.map(this::toKorrespondansepartResource)

    fun toKorrespondansepartResource(korrespondansepartDto: KorrespondansepartDto): KorrespondansepartResource =
        KorrespondansepartResource().apply {
            korrespondansepartDto.fodselsnummer?.let(::setFodselsnummer)
            korrespondansepartDto.organisasjonsnummer?.let(::setOrganisasjonsnummer)
            korrespondansepartDto.korrespondansepartNavn?.let(::setKorrespondansepartNavn)
            korrespondansepartDto.adresse?.let(adresseMappingService::toAdresseResource)?.let(::setAdresse)
            korrespondansepartDto.korrespondanseparttype?.let(Link::with)?.let(::addKorrespondanseparttype)
            korrespondansepartDto.kontaktperson?.let(::setKontaktperson)
            korrespondansepartDto.kontaktinformasjon
                ?.let(kontaktinformasjonMappingService::toKontaktinformasjon)
                ?.let(::setKontaktinformasjon)
            korrespondansepartDto.skjerming?.let(skjermingMappingService::toSkjermingResource)?.let(::setSkjerming)
        }
}
