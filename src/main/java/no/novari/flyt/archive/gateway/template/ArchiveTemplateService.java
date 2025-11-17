package no.novari.flyt.archive.gateway.template;

import no.novari.flyt.archive.gateway.dispatch.model.CaseDispatchType;
import no.novari.flyt.archive.gateway.template.model.Dependency;
import no.novari.flyt.archive.gateway.template.model.ElementConfig;
import no.novari.flyt.archive.gateway.template.model.MappingTemplate;
import no.novari.flyt.archive.gateway.template.model.ObjectTemplate;
import no.novari.flyt.archive.gateway.template.model.Selectable;
import no.novari.flyt.archive.gateway.template.model.SelectableValueTemplate;
import no.novari.flyt.archive.gateway.template.model.UrlBuilder;
import no.novari.flyt.archive.gateway.template.model.ValuePredicate;
import no.novari.flyt.archive.gateway.template.model.ValueTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ArchiveTemplateService {

    private final SearchParametersTemplateService searchParametersTemplateService;
    private final SakTemplateService sakTemplateService;
    private final JournalpostTemplateService journalpostTemplateService;

    public ArchiveTemplateService(
            SearchParametersTemplateService searchParametersTemplateService,
            SakTemplateService sakTemplateService,
            JournalpostTemplateService journalpostTemplateService
    ) {
        this.searchParametersTemplateService = searchParametersTemplateService;
        this.sakTemplateService = sakTemplateService;
        this.journalpostTemplateService = journalpostTemplateService;
    }

    public MappingTemplate createTemplate() {
        return MappingTemplate
                .builder()
                .displayName("Arkivering")
                .rootObjectTemplate(
                        ObjectTemplate
                                .builder()
                                .addTemplate(
                                        ElementConfig
                                                .builder()
                                                .key("type")
                                                .displayName("Sakslogikk")
                                                .description("""
                                                        Logikk for oppretting eller gjenfinning av sak.\
                                                        
                                                        Ved "Ny sak" opprettes det ny sak i arkivet.\
                                                        
                                                        Ved "På saksnummer" brukes eksisterende sak med innfyllt saksnummer.\
                                                        
                                                        Ved "På søk, eller ny" gjøres det først et forsøk på å finne saker\
                                                         som passer med valgte søkekriterier. \
                                                        
                                                        Søket har tre utfall: \
                                                        
                                                        \t(1) Ingen funn: Ny sak opprettes.\
                                                        
                                                        \t(2) Én sak funnet: Journalposter legges på funnet sak.\
                                                        
                                                        \t(3) To eller flere saker funnet: Instansprosessen feiler."""
                                                )
                                                .build(),
                                        SelectableValueTemplate
                                                .builder()
                                                .type(SelectableValueTemplate.Type.DROPDOWN)
                                                .selectables(List.of(
                                                        Selectable
                                                                .builder()
                                                                .displayName("Ny")
                                                                .value(CaseDispatchType.NEW.name())
                                                                .build(),
                                                        Selectable
                                                                .builder()
                                                                .displayName("På søk, eller ny")
                                                                .value(CaseDispatchType.BY_SEARCH_OR_NEW.name())
                                                                .build(),
                                                        Selectable
                                                                .builder()
                                                                .displayName("På saksnummer")
                                                                .value(CaseDispatchType.BY_ID.name())
                                                                .build()
                                                ))
                                                .build()
                                )
                                .addTemplate(
                                        ElementConfig
                                                .builder()
                                                .key("caseSearchParameters")
                                                .displayName("Søkeparametre")
                                                .description("Parametre for søk på sak. Huk av for de feltene som skal være med i søket. " +
                                                        "\nVerdiene for de avhukede feltene hentes fra oppsett for ny sak, så husk å fylle ut disse.")
                                                .showDependency(
                                                        Dependency
                                                                .builder()
                                                                .hasAnyCombination(List.of(
                                                                        List.of(ValuePredicate
                                                                                .builder()
                                                                                .key("type")
                                                                                .defined(true)
                                                                                .value(CaseDispatchType.BY_SEARCH_OR_NEW.name())
                                                                                .build()
                                                                        )
                                                                ))
                                                                .build()
                                                )
                                                .build(),
                                        searchParametersTemplateService.createTemplate()
                                )
                                .addTemplate(
                                        ElementConfig
                                                .builder()
                                                .key("newCase")
                                                .displayName("Sak")
                                                .description("Generisk sak")
                                                .showDependency(
                                                        Dependency
                                                                .builder()
                                                                .hasAnyCombination(List.of(
                                                                        List.of(
                                                                                ValuePredicate
                                                                                        .builder()
                                                                                        .key("type")
                                                                                        .defined(true)
                                                                                        .value(CaseDispatchType.NEW.name())
                                                                                        .build()
                                                                        ),
                                                                        List.of(
                                                                                ValuePredicate
                                                                                        .builder()
                                                                                        .key("type")
                                                                                        .defined(true)
                                                                                        .value(CaseDispatchType.BY_SEARCH_OR_NEW.name())
                                                                                        .build()
                                                                        )
                                                                ))
                                                                .build()
                                                )
                                                .build(),
                                        sakTemplateService.createTemplate()
                                )
                                .addTemplate(
                                        ElementConfig
                                                .builder()
                                                .key("caseId")
                                                .displayName("Saksnummer")
                                                .description("Entydig identifikasjon av mappen innenfor det arkivet mappen tilhører")
                                                .showDependency(
                                                        Dependency
                                                                .builder()
                                                                .hasAnyCombination(List.of(
                                                                        List.of(
                                                                                ValuePredicate
                                                                                        .builder()
                                                                                        .key("type")
                                                                                        .defined(true)
                                                                                        .value(CaseDispatchType.BY_ID.name())
                                                                                        .build()
                                                                        )
                                                                ))
                                                                .build()
                                                )
                                                .build(),
                                        ValueTemplate
                                                .builder()
                                                .type(ValueTemplate.Type.DYNAMIC_STRING)
                                                .search(
                                                        UrlBuilder
                                                                .builder()
                                                                .urlTemplate("api/intern/arkiv/saker/{caseId}/tittel")
                                                                .valueRefPerPathParamKey(Map.of(
                                                                        "caseId", "caseId"
                                                                ))
                                                                .build()
                                                )
                                                .build()
                                )
                                .addCollectionTemplate(
                                        ElementConfig
                                                .builder()
                                                .key("journalpost")
                                                .displayName("Journalposter")
                                                .description("Journalposter knyttet til saksmappe")
                                                .showDependency(
                                                        Dependency
                                                                .builder()
                                                                .hasAnyCombination(List.of(
                                                                        List.of(
                                                                                ValuePredicate
                                                                                        .builder()
                                                                                        .key("type")
                                                                                        .defined(true)
                                                                                        .value(CaseDispatchType.BY_ID.name())
                                                                                        .build()
                                                                        )
                                                                ))
                                                                .build()
                                                )
                                                .build(),
                                        journalpostTemplateService.createTemplate()
                                )
                                .build()
                )
                .build();
    }

}
