package no.fintlabs.configuration.template;

import no.fintlabs.configuration.template.model.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NySakTemplateService {

    private final KlasseringTemplateService klasseringTemplateService;
    private final SkjermingTemplateService skjermingTemplateService;
    private final JournalpostTemplateService journalpostTemplateService;

    public NySakTemplateService(
            KlasseringTemplateService klasseringTemplateService,
            SkjermingTemplateService skjermingTemplateService,
            JournalpostTemplateService journalpostTemplateService
    ) {
        this.klasseringTemplateService = klasseringTemplateService;
        this.skjermingTemplateService = skjermingTemplateService;
        this.journalpostTemplateService = journalpostTemplateService;
    }

    public ObjectTemplate createTemplate() {
        return ObjectTemplate
                .builder()
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("tittel")
                                .displayName("Tittel")
                                .description("")
                                .build(),
                        ValueTemplate
                                .builder()
                                .type(ValueTemplate.Type.DYNAMIC_STRING)
                                .build()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("offentligTittel")
                                .displayName("Offentlig tittel")
                                .description("")
                                .build(),
                        ValueTemplate
                                .builder()
                                .type(ValueTemplate.Type.DYNAMIC_STRING)
                                .build()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("saksmappetype")
                                .displayName("Saksmappetype")
                                .description("")
                                .build(),
                        SelectableValueTemplate
                                .builder()
                                .type(SelectableValueTemplate.Type.SEARCH_SELECT)
                                .selectablesSources(List.of(
                                        UrlBuilder.builder().urlTemplate("api/intern/arkiv/kodeverk/saksmappetype").build()
                                ))
                                .build()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("journalenhet")
                                .displayName("Journalenhet")
                                .description("")
                                .build(),
                        SelectableValueTemplate
                                .builder()
                                .type(SelectableValueTemplate.Type.SEARCH_SELECT)
                                .selectablesSources(List.of(
                                        UrlBuilder.builder().urlTemplate("api/intern/arkiv/kodeverk/administrativenhet").build()
                                ))
                                .build()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("administrativenhet")
                                .displayName("Administrativ enhet")
                                .description("")
                                .build(),
                        SelectableValueTemplate
                                .builder()
                                .type(SelectableValueTemplate.Type.SEARCH_SELECT)
                                .selectablesSources(List.of(
                                        UrlBuilder.builder().urlTemplate("api/intern/arkiv/kodeverk/administrativenhet").build()
                                ))
                                .build()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("saksansvarlig")
                                .displayName("Saksansvarlig")
                                .description("")
                                .build(),
                        SelectableValueTemplate
                                .builder()
                                .type(SelectableValueTemplate.Type.SEARCH_SELECT)
                                .selectablesSources(List.of(
                                        UrlBuilder.builder().urlTemplate("api/intern/arkiv/kodeverk/arkivressurs").build()
                                ))
                                .build()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("arkivdel")
                                .displayName("Arkivdel")
                                .description("")
                                .build(),
                        SelectableValueTemplate
                                .builder()
                                .type(SelectableValueTemplate.Type.SEARCH_SELECT)
                                .selectablesSources(List.of(
                                        UrlBuilder.builder().urlTemplate("api/intern/arkiv/kodeverk/arkivdel").build()
                                ))
                                .build()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("saksstatus")
                                .displayName("Saksstatus")
                                .description("")
                                .build(),
                        SelectableValueTemplate
                                .builder()
                                .type(SelectableValueTemplate.Type.SEARCH_SELECT)
                                .selectablesSources(List.of(
                                        UrlBuilder.builder().urlTemplate("api/intern/arkiv/kodeverk/saksstatus").build()
                                ))
                                .build()
                )
//                .addTemplate(
//                        ElementConfig
//                                .builder()
//                                .key("part")
//                                .displayName("Parter")
//                                .description("")
//                                .build(),
//                        ObjectCollectionTemplate
//                                .builder()
//                                .objectTemplate(partTemplateService.createTemplate())
//                                .build()
//                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("skjerming")
                                .displayName("Skjerming")
                                .description("")
                                .build(),
                        skjermingTemplateService.createTemplate()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("klassering")
                                .displayName("Klassering")
                                .description("")
                                .build(),
                        ObjectCollectionTemplate
                                .builder()
                                .elementTemplate(klasseringTemplateService.createTemplate())
                                .build()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("journalpost")
                                .displayName("Journalposter")
                                .description("")
                                .build(),
                        ObjectCollectionTemplate
                                .builder()
                                .elementTemplate(journalpostTemplateService.createTemplate())
                                .build()
                )
                .build();
    }

}