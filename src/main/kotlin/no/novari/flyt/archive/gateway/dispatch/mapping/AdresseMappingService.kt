package no.novari.flyt.archive.gateway.dispatch.mapping

import no.novari.fint.model.resource.felles.kompleksedatatyper.AdresseResource
import no.novari.flyt.archive.gateway.dispatch.model.instance.AdresseDto
import org.springframework.stereotype.Service

@Service
class AdresseMappingService {
    fun toAdresseResource(adresseDto: AdresseDto): AdresseResource =
        AdresseResource().apply {
            adresseDto.adresselinje?.let(::ArrayList)?.let(::setAdresselinje)
            adresseDto.postnummer?.let(::setPostnummer)
            adresseDto.poststed?.let(::setPoststed)
        }
}
