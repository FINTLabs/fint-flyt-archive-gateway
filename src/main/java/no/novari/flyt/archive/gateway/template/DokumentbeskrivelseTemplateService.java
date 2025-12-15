package no.novari.flyt.archive.gateway.template;

import no.novari.flyt.archive.gateway.template.model.ElementConfig;
import no.novari.flyt.archive.gateway.template.model.ObjectTemplate;
import no.novari.flyt.archive.gateway.template.model.SelectableValueTemplate;
import no.novari.flyt.archive.gateway.template.model.UrlBuilder;
import no.novari.flyt.archive.gateway.template.model.ValueTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DokumentbeskrivelseTemplateService {

    private final DokumentobjektTemplateService dokumentobjektTemplateService;
    private final SkjermingTemplateService skjermingTemplateService;

    public DokumentbeskrivelseTemplateService(
            DokumentobjektTemplateService dokumentobjektTemplateService,
            SkjermingTemplateService skjermingTemplateService
    ) {
        this.dokumentobjektTemplateService = dokumentobjektTemplateService;
        this.skjermingTemplateService = skjermingTemplateService;
    }

    public ObjectTemplate createTemplate() {
        return ObjectTemplate
                .builder()
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("tittel")
                                .displayName("Tittel")
                                .description("Tittel eller navn på arkivenheten")
                                .build(),
                        ValueTemplate
                                .builder()
                                .type(ValueTemplate.Type.DYNAMIC_STRING)
                                .build()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("dokumentstatus")
                                .displayName("Dokumentstatus")
                                .description("Status til dokumentet")
                                .build(),
                        SelectableValueTemplate
                                .builder()
                                .type(SelectableValueTemplate.Type.DYNAMIC_STRING_OR_SEARCH_SELECT)
                                .selectablesSources(List.of(
                                        UrlBuilder
                                                .builder()
                                                .urlTemplate("api/intern/arkiv/kodeverk/dokumentstatus")
                                                .build()
                                ))
                                .build()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("dokumentType")
                                .displayName("Dokumenttype")
                                .description("Navn på type dokument")
                                .build(),
                        SelectableValueTemplate
                                .builder()
                                .type(SelectableValueTemplate.Type.DYNAMIC_STRING_OR_SEARCH_SELECT)
                                .selectablesSources(List.of(
                                        UrlBuilder
                                                .builder()
                                                .urlTemplate("api/intern/arkiv/kodeverk/dokumenttype")
                                                .build()
                                ))
                                .build()

                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("tilknyttetRegistreringSom")
                                .displayName("Tilknyttet registrering som")
                                .description("Angivelse av hvilken \"rolle\" dokumentet har i forhold til registreringen")
                                .build(),
                        SelectableValueTemplate
                                .builder()
                                .type(SelectableValueTemplate.Type.DYNAMIC_STRING_OR_SEARCH_SELECT)
                                .selectablesSources(List.of(
                                        UrlBuilder
                                                .builder()
                                                .urlTemplate("api/intern/arkiv/kodeverk/tilknyttetregistreringsom")
                                                .build()
                                ))
                                .build()

                )
                .addCollectionTemplate(
                        ElementConfig
                                .builder()
                                .key("dokumentobjekt")
                                .displayName("Dokumentobjekter")
                                .description("Dokumentobjekt tilhørende dokumentbeskrivelsen")
                                .build(),
                        dokumentobjektTemplateService.createTemplate()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("skjerming")
                                .displayName("Skjerming")
                                .description("Skjerming av dokument")
                                .build(),
                        skjermingTemplateService.createTemplate()
                )
                .build();
    }

}
