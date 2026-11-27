package no.novari.flyt.archive.gateway.template

import no.novari.flyt.archive.gateway.dispatch.model.CaseDispatchType
import no.novari.flyt.archive.gateway.template.model.Dependency
import no.novari.flyt.archive.gateway.template.model.ElementConfig
import no.novari.flyt.archive.gateway.template.model.MappingTemplate
import no.novari.flyt.archive.gateway.template.model.ObjectTemplate
import no.novari.flyt.archive.gateway.template.model.Selectable
import no.novari.flyt.archive.gateway.template.model.SelectableValueTemplate
import no.novari.flyt.archive.gateway.template.model.UrlBuilder
import no.novari.flyt.archive.gateway.template.model.ValuePredicate
import no.novari.flyt.archive.gateway.template.model.ValueTemplate
import org.springframework.stereotype.Service

@Service
class ArchiveTemplateService(
    private val searchParametersTemplateService: SearchParametersTemplateService,
    private val sakTemplateService: SakTemplateService,
    private val journalpostTemplateService: JournalpostTemplateService,
) {
    fun createTemplate(): MappingTemplate =
        MappingTemplate(
            displayName = "Arkivering",
            rootObjectTemplate =
                ObjectTemplate
                    .builder()
                    .addTemplate(
                        ElementConfig(
                            key = "type",
                            displayName = "Sakslogikk",
                            description =
                                """
                                Logikk for oppretting eller gjenfinning av sak.
                                
                                Ved "Ny sak" opprettes det ny sak i arkivet.
                                
                                Ved "På saksnummer" brukes eksisterende sak med innfyllt saksnummer.
                                
                                Ved "På søk, eller ny" gjøres det først et forsøk på å finne saker som passer med valgte søkekriterier.
                                
                                Søket har tre utfall:
                                
                                	(1) Ingen funn: Ny sak opprettes.
                                
                                	(2) Én sak funnet: Journalposter legges på funnet sak.
                                
                                	(3) To eller flere saker funnet: Instansprosessen feiler.
                                """.trimIndent(),
                        ),
                        SelectableValueTemplate(
                            type = SelectableValueTemplate.Type.DROPDOWN,
                            selectables =
                                listOf(
                                    Selectable(
                                        displayName = "Ny",
                                        value = CaseDispatchType.NEW.name,
                                    ),
                                    Selectable(
                                        displayName = "På søk, eller ny",
                                        value = CaseDispatchType.BY_SEARCH_OR_NEW.name,
                                    ),
                                    Selectable(
                                        displayName = "På saksnummer",
                                        value = CaseDispatchType.BY_ID.name,
                                    ),
                                ),
                        ),
                    ).addTemplate(
                        ElementConfig(
                            key = "caseSearchParameters",
                            displayName = "Søkeparametre",
                            description =
                                "Parametre for søk på sak. Huk av for de feltene som skal være med i søket. " +
                                    "\nVerdiene for de avhukede feltene hentes fra oppsett for ny sak, så husk å " +
                                    "fylle ut disse.",
                            showDependency =
                                Dependency(
                                    hasAnyCombination =
                                        listOf(
                                            listOf(
                                                ValuePredicate(
                                                    key = "type",
                                                    defined = true,
                                                    value = CaseDispatchType.BY_SEARCH_OR_NEW.name,
                                                ),
                                            ),
                                        ),
                                ),
                        ),
                        searchParametersTemplateService.createTemplate(),
                    ).addTemplate(
                        ElementConfig(
                            key = "newCase",
                            displayName = "Sak",
                            description = "Generisk sak",
                            showDependency =
                                Dependency(
                                    hasAnyCombination =
                                        listOf(
                                            listOf(
                                                ValuePredicate(
                                                    key = "type",
                                                    defined = true,
                                                    value = CaseDispatchType.NEW.name,
                                                ),
                                            ),
                                            listOf(
                                                ValuePredicate(
                                                    key = "type",
                                                    defined = true,
                                                    value = CaseDispatchType.BY_SEARCH_OR_NEW.name,
                                                ),
                                            ),
                                        ),
                                ),
                        ),
                        sakTemplateService.createTemplate(),
                    ).addTemplate(
                        ElementConfig(
                            key = "caseId",
                            displayName = "Saksnummer",
                            description = "Entydig identifikasjon av mappen innenfor det arkivet mappen tilhører",
                            showDependency =
                                Dependency(
                                    hasAnyCombination =
                                        listOf(
                                            listOf(
                                                ValuePredicate(
                                                    key = "type",
                                                    defined = true,
                                                    value = CaseDispatchType.BY_ID.name,
                                                ),
                                            ),
                                        ),
                                ),
                        ),
                        ValueTemplate(
                            type = ValueTemplate.Type.DYNAMIC_STRING,
                            search =
                                UrlBuilder(
                                    urlTemplate = "api/intern/arkiv/saker/{caseId}/tittel",
                                    valueRefPerPathParamKey = mapOf("caseId" to "caseId"),
                                ),
                        ),
                    ).addCollectionTemplate(
                        ElementConfig(
                            key = "journalpost",
                            displayName = "Journalposter",
                            description = "Journalposter knyttet til saksmappe",
                            showDependency =
                                Dependency(
                                    hasAnyCombination =
                                        listOf(
                                            listOf(
                                                ValuePredicate(
                                                    key = "type",
                                                    defined = true,
                                                    value = CaseDispatchType.BY_ID.name,
                                                ),
                                            ),
                                        ),
                                ),
                        ),
                        journalpostTemplateService.createTemplate(),
                    ).build(),
        )
}
