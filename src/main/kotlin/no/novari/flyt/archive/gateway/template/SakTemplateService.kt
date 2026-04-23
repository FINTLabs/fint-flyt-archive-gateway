package no.novari.flyt.archive.gateway.template

import no.novari.flyt.archive.gateway.template.model.ElementConfig
import no.novari.flyt.archive.gateway.template.model.ObjectTemplate
import no.novari.flyt.archive.gateway.template.model.SelectableValueTemplate
import no.novari.flyt.archive.gateway.template.model.UrlBuilder
import no.novari.flyt.archive.gateway.template.model.ValueTemplate
import org.springframework.stereotype.Service

@Service
class SakTemplateService(
    private val klasseringTemplateService: KlasseringTemplateService,
    private val skjermingTemplateService: SkjermingTemplateService,
    private val journalpostTemplateService: JournalpostTemplateService,
    private val partTemplateService: PartTemplateService,
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
                    .key(
                        "saksmappetype",
                    ).displayName("Saksmappetype")
                    .description("Type saksmappe")
                    .build(),
                SelectableValueTemplate
                    .builder()
                    .type(SelectableValueTemplate.Type.DYNAMIC_STRING_OR_SEARCH_SELECT)
                    .selectablesSources(
                        listOf(UrlBuilder.builder().urlTemplate("api/intern/arkiv/kodeverk/saksmappetype").build()),
                    ).build(),
            ).addTemplate(
                ElementConfig
                    .builder()
                    .key("administrativEnhet")
                    .displayName("Administrativ enhet")
                    .description(
                        "Avdeling, kontor eller annen administrativ enhet som har ansvaret for saksbehandlingen.",
                    ).build(),
                SelectableValueTemplate
                    .builder()
                    .type(SelectableValueTemplate.Type.DYNAMIC_STRING_OR_SEARCH_SELECT)
                    .selectablesSources(
                        listOf(
                            UrlBuilder.builder().urlTemplate("api/intern/arkiv/kodeverk/administrativenhet").build(),
                        ),
                    ).build(),
            ).addTemplate(
                ElementConfig
                    .builder()
                    .key(
                        "saksansvarlig",
                    ).displayName("Saksansvarlig")
                    .description("Person som er saksansvarlig")
                    .build(),
                SelectableValueTemplate
                    .builder()
                    .type(SelectableValueTemplate.Type.DYNAMIC_STRING_OR_SEARCH_SELECT)
                    .selectablesSources(
                        listOf(UrlBuilder.builder().urlTemplate("api/intern/arkiv/kodeverk/arkivressurs").build()),
                    ).build(),
            ).addTemplate(
                ElementConfig
                    .builder()
                    .key(
                        "arkivdel",
                    ).displayName("Arkivdel")
                    .description("Arkivdel som mappe tilhører")
                    .build(),
                SelectableValueTemplate
                    .builder()
                    .type(SelectableValueTemplate.Type.DYNAMIC_STRING_OR_SEARCH_SELECT)
                    .selectablesSources(
                        listOf(UrlBuilder.builder().urlTemplate("api/intern/arkiv/kodeverk/arkivdel").build()),
                    ).build(),
            ).addTemplate(
                ElementConfig
                    .builder()
                    .key("saksstatus")
                    .displayName("Saksstatus")
                    .description(
                        "Status til saksmappen. Det vil si hvor langt saksbehandlingen har kommet. \n" +
                            "Registreres automatisk gjennom forskjellig saksbehandlingsfunksjonalitet, eller overstyres manuelt.",
                    ).build(),
                SelectableValueTemplate
                    .builder()
                    .type(SelectableValueTemplate.Type.DYNAMIC_STRING_OR_SEARCH_SELECT)
                    .selectablesSources(
                        listOf(UrlBuilder.builder().urlTemplate("api/intern/arkiv/kodeverk/sakstatus").build()),
                    ).build(),
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
                    ).build(),
            ).addCollectionTemplate(
                ElementConfig
                    .builder()
                    .key("part")
                    .displayName("Parter")
                    .description("Parter")
                    .build(),
                partTemplateService.createTemplate(),
            ).addTemplate(
                ElementConfig
                    .builder()
                    .key("skjerming")
                    .displayName("Skjerming")
                    .description(
                        "Skjerming benyttes til å skjerme registrerte opplysninger eller enkeltdokumenter. " +
                            "Skjermingen trer i kraft når en tilgangskode påføres den enkelte mappe, registrering eller det enkelte dokument.",
                    ).build(),
                skjermingTemplateService.createTemplate(),
            ).addCollectionTemplate(
                ElementConfig
                    .builder()
                    .key(
                        "klasse",
                    ).displayName("Klassering")
                    .description("Klassifisering av mappe")
                    .build(),
                klasseringTemplateService.createTemplate(),
            ).addCollectionTemplate(
                ElementConfig
                    .builder()
                    .key("journalpost")
                    .displayName("Journalposter")
                    .description(
                        "En journalpost representer en \"innføring i journalen\". \n" +
                            "Journalen er en kronologisk fortegnelse over inn- og utgående dokumenter " +
                            "(dvs. korrespondansedokumenter) brukt i saksbehandlingen, og eventuelt også interne dokumenter.",
                    ).build(),
                journalpostTemplateService.createTemplate(),
            ).build()
}
