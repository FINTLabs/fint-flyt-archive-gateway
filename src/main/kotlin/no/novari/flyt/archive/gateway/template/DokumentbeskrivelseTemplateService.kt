package no.novari.flyt.archive.gateway.template

import no.novari.fint.model.arkiv.kodeverk.DokumentStatus
import no.novari.fint.model.arkiv.kodeverk.DokumentType
import no.novari.fint.model.arkiv.kodeverk.TilknyttetRegistreringSom
import no.novari.flyt.archive.gateway.template.model.ElementConfig
import no.novari.flyt.archive.gateway.template.model.ObjectTemplate
import no.novari.flyt.archive.gateway.template.model.SelectableValueTemplate
import no.novari.flyt.archive.gateway.template.model.UrlBuilder
import no.novari.flyt.archive.gateway.template.model.ValueTemplate
import org.springframework.stereotype.Service

@Service
class DokumentbeskrivelseTemplateService(
    private val dokumentobjektTemplateService: DokumentobjektTemplateService,
    private val skjermingTemplateService: SkjermingTemplateService,
    private val referenceTemplateFactory: ReferenceTemplateFactory,
) {
    fun createTemplate(): ObjectTemplate =
        ObjectTemplate
            .builder()
            .addTemplate(
                ElementConfig(
                    key = "tittel",
                    displayName = "Tittel",
                    description = "Tittel eller navn på arkivenheten",
                ),
                ValueTemplate(
                    type = ValueTemplate.Type.DYNAMIC_STRING,
                ),
            ).addTemplate(
                ElementConfig(
                    key = "dokumentstatus",
                    displayName = "Dokumentstatus",
                    description = "Status til dokumentet",
                ),
                SelectableValueTemplate(
                    type = SelectableValueTemplate.Type.DYNAMIC_STRING_OR_SEARCH_SELECT,
                    selectablesSources =
                        listOf(
                            UrlBuilder(
                                urlTemplate = "api/intern/arkiv/kodeverk/dokumentstatus",
                            ),
                        ),
                    referenceTemplate = referenceTemplateFactory.create(DokumentStatus::class),
                ),
            ).addTemplate(
                ElementConfig(
                    key = "dokumentType",
                    displayName = "Dokumenttype",
                    description = "Navn på type dokument",
                ),
                SelectableValueTemplate(
                    type = SelectableValueTemplate.Type.DYNAMIC_STRING_OR_SEARCH_SELECT,
                    selectablesSources =
                        listOf(
                            UrlBuilder(
                                urlTemplate = "api/intern/arkiv/kodeverk/dokumenttype",
                            ),
                        ),
                    referenceTemplate = referenceTemplateFactory.create(DokumentType::class),
                ),
            ).addTemplate(
                ElementConfig(
                    key = "tilknyttetRegistreringSom",
                    displayName = "Tilknyttet registrering som",
                    description = "Angivelse av hvilken \"rolle\" dokumentet har i forhold til registreringen",
                ),
                SelectableValueTemplate(
                    type = SelectableValueTemplate.Type.DYNAMIC_STRING_OR_SEARCH_SELECT,
                    selectablesSources =
                        listOf(
                            UrlBuilder(
                                urlTemplate = "api/intern/arkiv/kodeverk/tilknyttetregistreringsom",
                            ),
                        ),
                    referenceTemplate = referenceTemplateFactory.create(TilknyttetRegistreringSom::class),
                ),
            ).addCollectionTemplate(
                ElementConfig(
                    key = "dokumentobjekt",
                    displayName = "Dokumentobjekter",
                    description = "Dokumentobjekt tilhørende dokumentbeskrivelsen",
                ),
                dokumentobjektTemplateService.createTemplate(),
            ).addTemplate(
                ElementConfig(
                    key = "skjerming",
                    displayName = "Skjerming",
                    description = "Skjerming av dokument",
                ),
                skjermingTemplateService.createTemplate(),
            ).build()
}
