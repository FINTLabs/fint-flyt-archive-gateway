package no.novari.flyt.archive.gateway.template

import no.novari.fint.model.arkiv.kodeverk.Saksmappetype
import no.novari.fint.model.arkiv.kodeverk.Saksstatus
import no.novari.fint.model.arkiv.kodeverk.Tilgangsgruppe
import no.novari.fint.model.arkiv.noark.AdministrativEnhet
import no.novari.fint.model.arkiv.noark.Arkivdel
import no.novari.fint.model.arkiv.noark.Arkivressurs
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
    private val referenceTemplateFactory: ReferenceTemplateFactory,
) {
    fun createTemplate(): ObjectTemplate =
        ObjectTemplate
            .builder()
            .addTemplate(
                ElementConfig(
                    key = "tittel",
                    displayName = "Tittel",
                    description = "Tittel",
                ),
                ValueTemplate(
                    type = ValueTemplate.Type.DYNAMIC_STRING,
                ),
            ).addTemplate(
                ElementConfig(
                    key = "offentligTittel",
                    displayName = "Offentlig tittel",
                    description = "Offentlig tittel. Husk å legge til eventuell skjerming.",
                ),
                ValueTemplate(
                    type = ValueTemplate.Type.DYNAMIC_STRING,
                ),
            ).addTemplate(
                ElementConfig(
                    key = "saksmappetype",
                    displayName = "Saksmappetype",
                    description = "Type saksmappe",
                ),
                SelectableValueTemplate(
                    type = SelectableValueTemplate.Type.DYNAMIC_STRING_OR_SEARCH_SELECT,
                    selectablesSources =
                        listOf(
                            UrlBuilder(
                                urlTemplate = "api/intern/arkiv/kodeverk/saksmappetype",
                            ),
                        ),
                    referenceTemplate = referenceTemplateFactory.create(Saksmappetype::class),
                ),
            ).addTemplate(
                ElementConfig(
                    key = "administrativEnhet",
                    displayName = "Administrativ enhet",
                    description =
                        "Avdeling, kontor eller annen administrativ enhet som har ansvaret for saksbehandlingen.",
                ),
                SelectableValueTemplate(
                    type = SelectableValueTemplate.Type.DYNAMIC_STRING_OR_SEARCH_SELECT,
                    selectablesSources =
                        listOf(
                            UrlBuilder(
                                urlTemplate = "api/intern/arkiv/kodeverk/administrativenhet",
                            ),
                        ),
                    referenceTemplate = referenceTemplateFactory.create(AdministrativEnhet::class),
                ),
            ).addTemplate(
                ElementConfig(
                    key = "saksansvarlig",
                    displayName = "Saksansvarlig",
                    description = "Person som er saksansvarlig",
                ),
                SelectableValueTemplate(
                    type = SelectableValueTemplate.Type.DYNAMIC_STRING_OR_SEARCH_SELECT,
                    selectablesSources =
                        listOf(
                            UrlBuilder(
                                urlTemplate = "api/intern/arkiv/kodeverk/arkivressurs",
                            ),
                        ),
                    referenceTemplate = referenceTemplateFactory.create(Arkivressurs::class),
                ),
            ).addTemplate(
                ElementConfig(
                    key = "arkivdel",
                    displayName = "Arkivdel",
                    description = "Arkivdel som mappe tilhører",
                ),
                SelectableValueTemplate(
                    type = SelectableValueTemplate.Type.DYNAMIC_STRING_OR_SEARCH_SELECT,
                    selectablesSources =
                        listOf(
                            UrlBuilder(
                                urlTemplate = "api/intern/arkiv/kodeverk/arkivdel",
                            ),
                        ),
                    referenceTemplate = referenceTemplateFactory.create(Arkivdel::class),
                ),
            ).addTemplate(
                ElementConfig(
                    key = "saksstatus",
                    displayName = "Saksstatus",
                    description =
                        "Status til saksmappen. Det vil si hvor langt saksbehandlingen har kommet. \n" +
                            "Registreres automatisk gjennom forskjellig saksbehandlingsfunksjonalitet, eller overstyres manuelt.",
                ),
                SelectableValueTemplate(
                    type = SelectableValueTemplate.Type.DYNAMIC_STRING_OR_SEARCH_SELECT,
                    selectablesSources =
                        listOf(
                            UrlBuilder(
                                urlTemplate = "api/intern/arkiv/kodeverk/sakstatus",
                            ),
                        ),
                    referenceTemplate = referenceTemplateFactory.create(Saksstatus::class),
                ),
            ).addTemplate(
                ElementConfig(
                    key = "tilgangsgruppe",
                    displayName = "Tilgangsgruppe",
                    description =
                        "Tilgangsgruppe gir mulighet for å skjerme innhold internt for andre brukere. (OBS! Dette " +
                            "feltet gjelder kun for p360 arkivsystem)",
                ),
                SelectableValueTemplate(
                    type = SelectableValueTemplate.Type.DYNAMIC_STRING_OR_SEARCH_SELECT,
                    selectablesSources =
                        listOf(
                            UrlBuilder(
                                urlTemplate = "api/intern/arkiv/kodeverk/tilgangsgruppe",
                            ),
                        ),
                    referenceTemplate = referenceTemplateFactory.create(Tilgangsgruppe::class),
                ),
            ).addCollectionTemplate(
                ElementConfig(
                    key = "part",
                    displayName = "Parter",
                    description = "Parter",
                ),
                partTemplateService.createTemplate(),
            ).addTemplate(
                ElementConfig(
                    key = "skjerming",
                    displayName = "Skjerming",
                    description =
                        "Skjerming benyttes til å skjerme registrerte opplysninger eller enkeltdokumenter. " +
                            "Skjermingen trer i kraft når en tilgangskode påføres den enkelte mappe, registrering eller det enkelte dokument.",
                ),
                skjermingTemplateService.createTemplate(),
            ).addCollectionTemplate(
                ElementConfig(
                    key = "klasse",
                    displayName = "Klassering",
                    description = "Klassifisering av mappe",
                ),
                klasseringTemplateService.createTemplate(),
            ).addCollectionTemplate(
                ElementConfig(
                    key = "journalpost",
                    displayName = "Journalposter",
                    description =
                        "En journalpost representer en \"innføring i journalen\". \n" +
                            "Journalen er en kronologisk fortegnelse over inn- og utgående dokumenter " +
                            "(dvs. korrespondansedokumenter) brukt i saksbehandlingen, og eventuelt også interne dokumenter.",
                ),
                journalpostTemplateService.createTemplate(),
            ).build()
}
