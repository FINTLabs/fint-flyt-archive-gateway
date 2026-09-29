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
                ElementConfig
                    .builder()
                    .key("tittel")
                    .displayName("Tittel")
                    .description("Tittel eller navn på arkivenheten")
                    .build(),
                ValueTemplate.builder().type(ValueTemplate.Type.DYNAMIC_STRING).build(),
            ).addTemplate(
                ElementConfig
                    .builder()
                    .key("dokumentstatus")
                    .displayName("Dokumentstatus")
                    .description("Status til dokumentet")
                    .build(),
                SelectableValueTemplate
                    .builder()
                    .type(SelectableValueTemplate.Type.DYNAMIC_STRING_OR_SEARCH_SELECT)
                    .selectablesSources(
                        listOf(UrlBuilder.builder().urlTemplate("api/intern/arkiv/kodeverk/dokumentstatus").build()),
                    ).referenceTemplate(referenceTemplateFactory.create(DokumentStatus::class))
                    .build(),
            ).addTemplate(
                ElementConfig
                    .builder()
                    .key("dokumentType")
                    .displayName("Dokumenttype")
                    .description("Navn på type dokument")
                    .build(),
                SelectableValueTemplate
                    .builder()
                    .type(SelectableValueTemplate.Type.DYNAMIC_STRING_OR_SEARCH_SELECT)
                    .selectablesSources(
                        listOf(UrlBuilder.builder().urlTemplate("api/intern/arkiv/kodeverk/dokumenttype").build()),
                    ).referenceTemplate(referenceTemplateFactory.create(DokumentType::class))
                    .build(),
            ).addTemplate(
                ElementConfig
                    .builder()
                    .key("tilknyttetRegistreringSom")
                    .displayName("Tilknyttet registrering som")
                    .description("Angivelse av hvilken \"rolle\" dokumentet har i forhold til registreringen")
                    .build(),
                SelectableValueTemplate
                    .builder()
                    .type(SelectableValueTemplate.Type.DYNAMIC_STRING_OR_SEARCH_SELECT)
                    .selectablesSources(
                        listOf(
                            UrlBuilder
                                .builder()
                                .urlTemplate(
                                    "api/intern/arkiv/kodeverk/tilknyttetregistreringsom",
                                ).build(),
                        ),
                    ).referenceTemplate(referenceTemplateFactory.create(TilknyttetRegistreringSom::class))
                    .build(),
            ).addCollectionTemplate(
                ElementConfig
                    .builder()
                    .key("dokumentobjekt")
                    .displayName("Dokumentobjekter")
                    .description("Dokumentobjekt tilhørende dokumentbeskrivelsen")
                    .build(),
                dokumentobjektTemplateService.createTemplate(),
            ).addTemplate(
                ElementConfig
                    .builder()
                    .key(
                        "skjerming",
                    ).displayName("Skjerming")
                    .description("Skjerming av dokument")
                    .build(),
                skjermingTemplateService.createTemplate(),
            ).build()
}
