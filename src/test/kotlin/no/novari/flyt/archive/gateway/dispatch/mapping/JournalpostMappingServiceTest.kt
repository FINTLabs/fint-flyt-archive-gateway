package no.novari.flyt.archive.gateway.dispatch.mapping

import no.novari.fint.model.resource.Link
import no.novari.flyt.archive.gateway.dispatch.model.instance.DokumentbeskrivelseDto
import no.novari.flyt.archive.gateway.dispatch.model.instance.JournalpostDto
import no.novari.flyt.archive.gateway.dispatch.model.instance.KorrespondansepartDto
import no.novari.flyt.archive.gateway.dispatch.model.instance.SkjermingDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class JournalpostMappingServiceTest {
    private val journalpostMappingService =
        JournalpostMappingService(
            SkjermingMappingService(),
            KorrespondansepartMappingService(
                AdresseMappingService(),
                KontaktinformasjonMappingService(),
                SkjermingMappingService(),
            ),
            DokumentbeskrivelseMappingService(DokumentObjektMappingService(), SkjermingMappingService()),
        )

    @Test
    fun `maps to JournalpostResource`() {
        val skjermingDto =
            SkjermingDto
                .builder()
                .tilgangsrestriksjon("Tilgangsrestriksjon")
                .skjermingshjemmel("Skjermingshjemmel")
                .build()

        val dto =
            JournalpostDto
                .builder()
                .tittel("Tittel")
                .offentligTittel("Offentlig tittel")
                .journalposttype("Journalpost Type")
                .administrativEnhet("Administrativ enhet")
                .saksbehandler("Saksbehandler")
                .journalstatus("Journalstatus")
                .tilgangsgruppe("Tilgangsgruppe")
                .skjerming(skjermingDto)
                .korrespondansepart(listOf(KorrespondansepartDto.builder().korrespondanseparttype("type").build()))
                .dokumentbeskrivelse(listOf(DokumentbeskrivelseDto.builder().tittel("dok").build()))
                .build()

        val mappedResource = journalpostMappingService.toJournalpostResource(dto, emptyMap())

        assertThat(mappedResource.tittel).isEqualTo("Tittel")
        assertThat(mappedResource.offentligTittel).isEqualTo("Offentlig tittel")
        assertThat(mappedResource.journalposttype.first().href).isEqualTo("Journalpost Type")
        assertThat(mappedResource.administrativEnhet.first().href).isEqualTo("Administrativ enhet")
        assertThat(mappedResource.saksbehandler.first().href).isEqualTo("Saksbehandler")
        assertThat(mappedResource.journalstatus.first().href).isEqualTo("Journalstatus")
        assertThat(mappedResource.tilgangsgruppe.first().href).isEqualTo("Tilgangsgruppe")
        assertThat(mappedResource.skjerming.tilgangsrestriksjon).contains(Link.with("Tilgangsrestriksjon"))
        assertThat(mappedResource.skjerming.skjermingshjemmel).contains(Link.with("Skjermingshjemmel"))
    }
}
