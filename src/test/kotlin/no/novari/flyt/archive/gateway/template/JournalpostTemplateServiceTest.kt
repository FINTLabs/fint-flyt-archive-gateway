package no.novari.flyt.archive.gateway.template

import no.novari.flyt.archive.gateway.template.model.ValueTemplate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class JournalpostTemplateServiceTest {
    private val journalpostTemplateService =
        JournalpostTemplateService(
            KorrespondansepartTemplateService(
                AdresseTemplateService(),
                KontaktinformasjonTemplateService(),
                SkjermingTemplateService(),
            ),
            DokumentbeskrivelseTemplateService(
                DokumentobjektTemplateService(),
                SkjermingTemplateService(),
            ),
            SkjermingTemplateService(),
        )

    @Test
    fun `template explains required dokumentetsDato format`() {
        val template = journalpostTemplateService.createTemplate()

        val dokumentetsDatoTemplate =
            template.valueTemplates.single { it.elementConfig?.key == "dokumentetsDato" }

        assertThat(dokumentetsDatoTemplate.elementConfig?.displayName).isEqualTo("Dokumentets dato")
        assertThat(dokumentetsDatoTemplate.elementConfig?.description)
            .isEqualTo(
                "Dato som er påført selve dokumentet. Må være på ISO 8601-format YYYY-MM-DD eller YYYY-MM-DDThh:mm:ssZ.",
            )
        assertThat(dokumentetsDatoTemplate.template?.type).isEqualTo(ValueTemplate.Type.DYNAMIC_STRING)
    }
}
