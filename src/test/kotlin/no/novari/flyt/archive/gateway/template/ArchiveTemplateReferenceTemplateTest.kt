package no.novari.flyt.archive.gateway.template

import no.novari.flyt.archive.gateway.template.model.ObjectTemplate
import no.novari.flyt.archive.gateway.template.model.SelectableValueTemplate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ArchiveTemplateReferenceTemplateTest {
    private val template = TestTemplateServices.archiveTemplateService().createTemplate()

    @Test
    fun `every selectable backed by a codelist has a reference template`() {
        val codelistSelectables =
            selectableValueTemplates(requireNotNull(template.rootObjectTemplate))
                .filter { selectable ->
                    selectable.selectablesSources.orEmpty().any {
                        it.urlTemplate!!.startsWith("api/intern/arkiv/kodeverk/") &&
                            it.urlTemplate != "api/intern/arkiv/kodeverk/klasse"
                    }
                }

        assertThat(codelistSelectables).isNotEmpty.allSatisfy { assertThat(it.referenceTemplate).isNotNull }
    }

    @Test
    fun `reference templates cover all codelist resources`() {
        val resourcePaths =
            selectableValueTemplates(requireNotNull(template.rootObjectTemplate))
                .mapNotNull { it.referenceTemplate?.urlTemplate }
                .map {
                    it
                        .removePrefix("${TestTemplateServices.FINT_BASE_URL}/")
                        .removeSuffix("/{identifierType}/{identifierValue}")
                }.toSet()

        assertThat(resourcePaths).containsExactlyInAnyOrder(
            "arkiv/noark/administrativenhet",
            "arkiv/noark/arkivdel",
            "arkiv/noark/arkivressurs",
            "arkiv/noark/klassifikasjonssystem",
            "arkiv/kodeverk/dokumentstatus",
            "arkiv/kodeverk/dokumenttype",
            "arkiv/kodeverk/format",
            "arkiv/kodeverk/journalposttype",
            "arkiv/kodeverk/journalstatus",
            "arkiv/kodeverk/korrespondanseparttype",
            "arkiv/kodeverk/partrolle",
            "arkiv/kodeverk/saksmappetype",
            "arkiv/kodeverk/saksstatus",
            "arkiv/kodeverk/skjermingshjemmel",
            "arkiv/kodeverk/tilgangsgruppe",
            "arkiv/kodeverk/tilgangsrestriksjon",
            "arkiv/kodeverk/tilknyttetregistreringsom",
            "arkiv/kodeverk/variantformat",
        )
    }

    private fun selectableValueTemplates(objectTemplate: ObjectTemplate): List<SelectableValueTemplate> =
        objectTemplate.selectableValueTemplates.mapNotNull { it.template } +
            objectTemplate.objectTemplates.mapNotNull { it.template }.flatMap(::selectableValueTemplates) +
            objectTemplate.objectCollectionTemplates
                .mapNotNull { it.template?.elementTemplate }
                .flatMap(::selectableValueTemplates)
}
