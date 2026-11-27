package no.novari.flyt.archive.gateway.template

import no.novari.fint.model.arkiv.kodeverk.Format
import no.novari.fint.model.arkiv.kodeverk.Variantformat
import no.novari.flyt.archive.gateway.template.model.ElementConfig
import no.novari.flyt.archive.gateway.template.model.ObjectTemplate
import no.novari.flyt.archive.gateway.template.model.SelectableValueTemplate
import no.novari.flyt.archive.gateway.template.model.UrlBuilder
import no.novari.flyt.archive.gateway.template.model.ValueTemplate
import org.springframework.stereotype.Service

@Service
class DokumentobjektTemplateService(
    private val referenceTemplateFactory: ReferenceTemplateFactory,
) {
    fun createTemplate(): ObjectTemplate =
        ObjectTemplate
            .builder()
            .addTemplate(
                ElementConfig(
                    key = "variantformat",
                    displayName = "Variantformat",
                    description = "Angivelse av hvilken variant et dokument forekommer i",
                ),
                SelectableValueTemplate(
                    type = SelectableValueTemplate.Type.DYNAMIC_STRING_OR_SEARCH_SELECT,
                    selectablesSources =
                        listOf(
                            UrlBuilder(
                                urlTemplate = "api/intern/arkiv/kodeverk/variantformat",
                            ),
                        ),
                    referenceTemplate = referenceTemplateFactory.create(Variantformat::class),
                ),
            ).addTemplate(
                ElementConfig(
                    key = "filformat",
                    displayName = "Filformat",
                    description = "Dokumentets format",
                ),
                SelectableValueTemplate(
                    type = SelectableValueTemplate.Type.DYNAMIC_STRING_OR_SEARCH_SELECT,
                    selectablesSources =
                        listOf(
                            UrlBuilder(
                                urlTemplate = "api/intern/arkiv/kodeverk/format",
                            ),
                        ),
                    referenceTemplate = referenceTemplateFactory.create(Format::class),
                ),
            ).addTemplate(
                ElementConfig(
                    key = "fil",
                    displayName = "Fil",
                    description =
                        "Referanse til filen som inneholder det elektroniske dokumentet som dokumentobjektet beskriver",
                ),
                ValueTemplate(
                    type = ValueTemplate.Type.FILE,
                ),
            ).build()
}
