package no.novari.flyt.archive.gateway.template

import no.novari.fint.model.arkiv.kodeverk.Format
import no.novari.fint.model.arkiv.noark.Arkivressurs
import no.novari.flyt.archive.gateway.template.model.ReferenceTemplate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ReferenceTemplateFactoryTest {
    private val referenceTemplateFactory = ReferenceTemplateFactory("https://api.felleskomponent.no")

    @Test
    fun `creates reference template for kodeverk resource with only systemid`() {
        val referenceTemplate = referenceTemplateFactory.create(Format::class)

        assertThat(referenceTemplate).isEqualTo(
            ReferenceTemplate(
                urlTemplate = "https://api.felleskomponent.no/arkiv/kodeverk/format/{identifierType}/{identifierValue}",
                identifierTypes = listOf("systemid"),
            ),
        )
    }

    @Test
    fun `creates reference template for arkivressurs with systemid and kildesystemid`() {
        val referenceTemplate = referenceTemplateFactory.create(Arkivressurs::class)

        assertThat(referenceTemplate.urlTemplate)
            .isEqualTo("https://api.felleskomponent.no/arkiv/noark/arkivressurs/{identifierType}/{identifierValue}")
        assertThat(referenceTemplate.identifierTypes).containsExactlyInAnyOrder("systemid", "kildesystemid")
    }

    @Test
    fun `ignores trailing slash in base url`() {
        val referenceTemplate = ReferenceTemplateFactory("https://beta.felleskomponent.no/").create(Format::class)

        assertThat(referenceTemplate.urlTemplate)
            .isEqualTo("https://beta.felleskomponent.no/arkiv/kodeverk/format/{identifierType}/{identifierValue}")
    }
}
