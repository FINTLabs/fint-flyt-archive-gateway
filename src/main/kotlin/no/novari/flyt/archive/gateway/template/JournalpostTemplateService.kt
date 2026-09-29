package no.novari.flyt.archive.gateway.template

import no.novari.fint.model.arkiv.kodeverk.JournalStatus
import no.novari.fint.model.arkiv.kodeverk.JournalpostType
import no.novari.fint.model.arkiv.kodeverk.Tilgangsgruppe
import no.novari.fint.model.arkiv.noark.AdministrativEnhet
import no.novari.fint.model.arkiv.noark.Arkivressurs
import no.novari.flyt.archive.gateway.template.model.ElementConfig
import no.novari.flyt.archive.gateway.template.model.ObjectTemplate
import no.novari.flyt.archive.gateway.template.model.SelectableValueTemplate
import no.novari.flyt.archive.gateway.template.model.UrlBuilder
import no.novari.flyt.archive.gateway.template.model.ValueTemplate
import org.springframework.stereotype.Service

@Service
class JournalpostTemplateService(
    private val korrespondansepartTemplateService: KorrespondansepartTemplateService,
    private val dokumentbeskrivelseTemplateService: DokumentbeskrivelseTemplateService,
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
                    .description("Tittel")
                    .build(),
                ValueTemplate.builder().type(ValueTemplate.Type.DYNAMIC_STRING).build(),
            ).addTemplate(
                ElementConfig
                    .builder()
                    .key("offentligTittel")
                    .displayName("Offentlig tittel")
                    .description("Offentlig tittel. Husk å legge til eventuell skjerming.")
                    .build(),
                ValueTemplate.builder().type(ValueTemplate.Type.DYNAMIC_STRING).build(),
            ).addTemplate(
                ElementConfig
                    .builder()
                    .key("journalposttype")
                    .displayName("Journalposttype")
                    .description("Navn på type journalpost. Tilsvarer \"Noark dokumenttype\" i Noark 4")
                    .build(),
                SelectableValueTemplate
                    .builder()
                    .type(SelectableValueTemplate.Type.DYNAMIC_STRING_OR_SEARCH_SELECT)
                    .selectablesSources(
                        listOf(UrlBuilder.builder().urlTemplate("api/intern/arkiv/kodeverk/journalposttype").build()),
                    ).referenceTemplate(referenceTemplateFactory.create(JournalpostType::class))
                    .build(),
            ).addTemplate(
                ElementConfig
                    .builder()
                    .key("administrativEnhet")
                    .displayName("Administrativ enhet")
                    .description(
                        "Navn på avdeling, kontor eller annen administrativ enhet som har \nansvaret for saksbehandlingen",
                    ).build(),
                SelectableValueTemplate
                    .builder()
                    .type(SelectableValueTemplate.Type.DYNAMIC_STRING_OR_SEARCH_SELECT)
                    .selectablesSources(
                        listOf(
                            UrlBuilder.builder().urlTemplate("api/intern/arkiv/kodeverk/administrativenhet").build(),
                        ),
                    ).referenceTemplate(referenceTemplateFactory.create(AdministrativEnhet::class))
                    .build(),
            ).addTemplate(
                ElementConfig
                    .builder()
                    .key(
                        "saksbehandler",
                    ).displayName("Saksbehandler")
                    .description("Navn på person som er saksbehandler")
                    .build(),
                SelectableValueTemplate
                    .builder()
                    .type(SelectableValueTemplate.Type.DYNAMIC_STRING_OR_SEARCH_SELECT)
                    .selectablesSources(
                        listOf(UrlBuilder.builder().urlTemplate("api/intern/arkiv/kodeverk/arkivressurs").build()),
                    ).referenceTemplate(referenceTemplateFactory.create(Arkivressurs::class))
                    .build(),
            ).addTemplate(
                ElementConfig
                    .builder()
                    .key(
                        "journalstatus",
                    ).displayName("Journalstatus")
                    .description("Status for journalposten")
                    .build(),
                SelectableValueTemplate
                    .builder()
                    .type(SelectableValueTemplate.Type.DYNAMIC_STRING_OR_SEARCH_SELECT)
                    .selectablesSources(
                        listOf(UrlBuilder.builder().urlTemplate("api/intern/arkiv/kodeverk/journalstatus").build()),
                    ).referenceTemplate(referenceTemplateFactory.create(JournalStatus::class))
                    .build(),
            ).addTemplate(
                ElementConfig
                    .builder()
                    .key("dokumentetsDato")
                    .displayName("Dokumentets dato")
                    .description(
                        "Dato som er påført selve dokumentet. Må være på ISO 8601-format YYYY-MM-DD.",
                    ).build(),
                ValueTemplate.builder().type(ValueTemplate.Type.DYNAMIC_STRING).build(),
            ).addTemplate(
                ElementConfig
                    .builder()
                    .key("tilgangsgruppe")
                    .displayName("Tilgangsgruppe")
                    .description(
                        "Tilgangsgruppe gir mulighet for å skjerme innhold internt for andre brukere. (OBS! Dette feltet gjelder kun for p360 arkivsystem)",
                    ).build(),
                SelectableValueTemplate
                    .builder()
                    .type(SelectableValueTemplate.Type.DYNAMIC_STRING_OR_SEARCH_SELECT)
                    .selectablesSources(
                        listOf(UrlBuilder.builder().urlTemplate("api/intern/arkiv/kodeverk/tilgangsgruppe").build()),
                    ).referenceTemplate(referenceTemplateFactory.create(Tilgangsgruppe::class))
                    .build(),
            ).addTemplate(
                ElementConfig
                    .builder()
                    .key(
                        "skjerming",
                    ).displayName("Skjerming")
                    .description("Skjerming av registrering")
                    .build(),
                skjermingTemplateService.createTemplate(),
            ).addCollectionTemplate(
                ElementConfig
                    .builder()
                    .key("korrespondansepart")
                    .displayName("Korrespondanseparter")
                    .description("Mottaker eller sender av arkivdokumenter.")
                    .build(),
                korrespondansepartTemplateService.createTemplate(),
            ).addCollectionTemplate(
                ElementConfig
                    .builder()
                    .key("dokumentbeskrivelse")
                    .displayName("Dokumentbeskrivelser")
                    .description("Dokumentbeskrivelsene til en registrering")
                    .build(),
                dokumentbeskrivelseTemplateService.createTemplate(),
            ).build()
}
