package no.novari.flyt.archive.gateway.template

import no.novari.flyt.archive.gateway.template.model.Selectable
import no.novari.flyt.archive.gateway.template.model.SelectableValueTemplate
import no.novari.flyt.archive.gateway.template.model.ValueTemplate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class JournalpostTemplateServiceTest {
    private val journalpostTemplateService = TestTemplateServices.journalpostTemplateService()

    @Test
    fun `template offers fixed avskrivningsmate values without resource lookup`() {
        val template = journalpostTemplateService.createTemplate()
        val avskrivningsmate =
            template.selectableValueTemplates.single { it.elementConfig?.key == "avskrivningsmate" }
        val journalposttype = template.selectableValueTemplates.single { it.elementConfig?.key == "journalposttype" }
        val administrativEnhet =
            template.selectableValueTemplates.single { it.elementConfig?.key == "administrativEnhet" }

        assertThat(avskrivningsmate.elementConfig?.displayName).isEqualTo("Avskrivningsmåte")
        assertThat(avskrivningsmate.elementConfig?.description).contains("I (Inngående brev)")
        assertThat(avskrivningsmate.order).isEqualTo(journalposttype.order + 1)
        assertThat(administrativEnhet.order).isEqualTo(avskrivningsmate.order + 1)
        assertThat(avskrivningsmate.template?.type)
            .isEqualTo(SelectableValueTemplate.Type.DYNAMIC_STRING_OR_SEARCH_SELECT)
        assertThat(avskrivningsmate.template?.selectables).containsExactly(
            Selectable(displayName = "[BU] Besvart med brev", value = "BU"),
            Selectable(displayName = "[BE] Besvart med e-post", value = "BE"),
            Selectable(displayName = "[TLF] Besvart på telefon", value = "TLF"),
            Selectable(displayName = "[TE] Tatt til etterretning", value = "TE"),
            Selectable(displayName = "[TO] Tatt til orientering", value = "TO"),
        )
        assertThat(avskrivningsmate.template?.selectablesSources).isNull()
        assertThat(avskrivningsmate.template?.referenceTemplate).isNull()
    }

    @Test
    fun `template explains required dokumentetsDato format`() {
        val template = journalpostTemplateService.createTemplate()

        val dokumentetsDatoTemplate =
            template.valueTemplates.single { it.elementConfig?.key == "dokumentetsDato" }

        assertThat(dokumentetsDatoTemplate.elementConfig?.displayName).isEqualTo("Dokumentets dato")
        assertThat(dokumentetsDatoTemplate.elementConfig?.description)
            .isEqualTo(
                "Dato som er påført selve dokumentet. Må være på ISO 8601-format YYYY-MM-DD.",
            )
        assertThat(dokumentetsDatoTemplate.template?.type).isEqualTo(ValueTemplate.Type.DYNAMIC_STRING)
    }
}
