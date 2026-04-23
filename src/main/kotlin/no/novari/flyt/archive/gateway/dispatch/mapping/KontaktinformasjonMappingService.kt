package no.novari.flyt.archive.gateway.dispatch.mapping

import no.novari.fint.model.felles.kompleksedatatyper.Kontaktinformasjon
import no.novari.flyt.archive.gateway.dispatch.model.instance.KontaktinformasjonDto
import org.springframework.stereotype.Service

@Service
class KontaktinformasjonMappingService {
    fun toKontaktinformasjon(kontaktinformasjonDto: KontaktinformasjonDto?): Kontaktinformasjon? =
        kontaktinformasjonDto?.let {
            Kontaktinformasjon().apply {
                kontaktinformasjonDto.epostadresse?.let(::setEpostadresse)
                kontaktinformasjonDto.telefonnummer?.let(::setTelefonnummer)
                kontaktinformasjonDto.mobiltelefonnummer?.let(::setMobiltelefonnummer)
            }
        }
}
