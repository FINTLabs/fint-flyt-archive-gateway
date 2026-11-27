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
    private val avskrivningTemplateService: AvskrivningTemplateService,
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
                    key = "journalposttype",
                    displayName = "Journalposttype",
                    description = "Navn på type journalpost. Tilsvarer \"Noark dokumenttype\" i Noark 4",
                ),
                SelectableValueTemplate(
                    type = SelectableValueTemplate.Type.DYNAMIC_STRING_OR_SEARCH_SELECT,
                    selectablesSources =
                        listOf(
                            UrlBuilder(
                                urlTemplate = "api/intern/arkiv/kodeverk/journalposttype",
                            ),
                        ),
                    referenceTemplate = referenceTemplateFactory.create(JournalpostType::class),
                ),
            ).addTemplate(
                ElementConfig(
                    key = "administrativEnhet",
                    displayName = "Administrativ enhet",
                    description =
                        "Navn på avdeling, kontor eller annen administrativ enhet som har \nansvaret for " +
                            "saksbehandlingen",
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
                    key = "saksbehandler",
                    displayName = "Saksbehandler",
                    description = "Navn på person som er saksbehandler",
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
                    key = "journalstatus",
                    displayName = "Journalstatus",
                    description = "Status for journalposten",
                ),
                SelectableValueTemplate(
                    type = SelectableValueTemplate.Type.DYNAMIC_STRING_OR_SEARCH_SELECT,
                    selectablesSources =
                        listOf(
                            UrlBuilder(
                                urlTemplate = "api/intern/arkiv/kodeverk/journalstatus",
                            ),
                        ),
                    referenceTemplate = referenceTemplateFactory.create(JournalStatus::class),
                ),
            ).addTemplate(
                ElementConfig(
                    key = "dokumentetsDato",
                    displayName = "Dokumentets dato",
                    description = "Dato som er påført selve dokumentet. Må være på ISO 8601-format YYYY-MM-DD.",
                ),
                ValueTemplate(
                    type = ValueTemplate.Type.DYNAMIC_STRING,
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
            ).addTemplate(
                ElementConfig(
                    key = "avskrivning",
                    displayName = "Avskrivning",
                    description =
                        "Avskrivning av journalpost. Gjelder kun når journalposttype er I (Inngående brev). " +
                            "For andre journalposttyper utelates hele avskrivningen ved utsending til arkivet. " +
                            "Avskrivningsmåte må være fylt ut. Hvis avskrivningsdato er tom, brukes dagens dato " +
                            "og klokkeslett ved utsending til arkivet.",
                ),
                avskrivningTemplateService.createTemplate(),
            ).addTemplate(
                ElementConfig(
                    key = "skjerming",
                    displayName = "Skjerming",
                    description = "Skjerming av registrering",
                ),
                skjermingTemplateService.createTemplate(),
            ).addCollectionTemplate(
                ElementConfig(
                    key = "korrespondansepart",
                    displayName = "Korrespondanseparter",
                    description = "Mottaker eller sender av arkivdokumenter.",
                ),
                korrespondansepartTemplateService.createTemplate(),
            ).addCollectionTemplate(
                ElementConfig(
                    key = "dokumentbeskrivelse",
                    displayName = "Dokumentbeskrivelser",
                    description = "Dokumentbeskrivelsene til en registrering",
                ),
                dokumentbeskrivelseTemplateService.createTemplate(),
            ).build()
}
