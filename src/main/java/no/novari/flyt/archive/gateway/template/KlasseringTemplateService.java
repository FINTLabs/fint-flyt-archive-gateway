package no.novari.flyt.archive.gateway.template;

import no.novari.flyt.archive.gateway.template.model.ElementConfig;
import no.novari.flyt.archive.gateway.template.model.ObjectTemplate;
import no.novari.flyt.archive.gateway.template.model.SelectableValueTemplate;
import no.novari.flyt.archive.gateway.template.model.UrlBuilder;
import no.novari.flyt.archive.gateway.template.model.ValueTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class KlasseringTemplateService {

    private final SkjermingTemplateService skjermingTemplateService;

    public KlasseringTemplateService(SkjermingTemplateService skjermingTemplateService) {
        this.skjermingTemplateService = skjermingTemplateService;
    }

    public ObjectTemplate createTemplate() {
        return ObjectTemplate
                .builder()
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("rekkefolge")
                                .displayName("Rekkefølge")
                                .description("""
                                        Rekkefølge for klassifiseringer.\s
                                        Ved bruk av primær, sekundær og tertiærklasseringer, \
                                        bruk følgende verdier:\s
                                        1 for primær, 2 for sekundær, og 3 for tertiær.""")
                                .build(),
                        ValueTemplate
                                .builder()
                                .type(ValueTemplate.Type.STRING)
                                .build()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("klassifikasjonssystem")
                                .displayName("Klassifikasjonssystem")
                                .description("Beskriver den overordnede strukturen for mappene i en eller flere arkivdeler")
                                .build(),
                        SelectableValueTemplate
                                .builder()
                                .type(SelectableValueTemplate.Type.DYNAMIC_STRING_OR_SEARCH_SELECT)
                                .selectablesSources(List.of(
                                        UrlBuilder.builder().urlTemplate("api/intern/arkiv/kodeverk/klassifikasjonssystem").build()
                                ))
                                .build()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("klasseId")
                                .displayName("KlasseID")
                                .description("Entydig identifikasjon av klassen innenfor klassifikasjonssystemet")
                                .build(),
                        SelectableValueTemplate
                                .builder()
                                .type(SelectableValueTemplate.Type.DYNAMIC_STRING_OR_SEARCH_SELECT)
                                .selectablesSources(List.of(
                                        UrlBuilder
                                                .builder()
                                                .urlTemplate("api/intern/arkiv/kodeverk/klasse")
                                                .valueRefPerRequestParamKey(Map.of(
                                                        "klassifikasjonssystemLink", "klassifikasjonssystem"
                                                ))
                                                .build()
                                ))
                                .build()
                )
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
                                .key("skjerming")
                                .displayName("Skjerming")
                                .description("Skjerming av klasse")
                                .build(),
                        skjermingTemplateService.createTemplate()
                )
                .build();
    }

}
