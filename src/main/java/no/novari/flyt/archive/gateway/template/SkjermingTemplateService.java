package no.novari.flyt.archive.gateway.template;

import no.novari.flyt.archive.gateway.template.model.ElementConfig;
import no.novari.flyt.archive.gateway.template.model.ObjectTemplate;
import no.novari.flyt.archive.gateway.template.model.SelectableValueTemplate;
import no.novari.flyt.archive.gateway.template.model.UrlBuilder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SkjermingTemplateService {

    public ObjectTemplate createTemplate() {
        return ObjectTemplate
                .builder()
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("tilgangsrestriksjon")
                                .displayName("Tilgangsrestriksjon")
                                .description("Angivelse av at dokumentene som tilhører arkivenheten ikke er offentlig " +
                                        "\ntilgjengelig i henhold til offentlighetsloven eller av en annen grunn")
                                .build(),
                        SelectableValueTemplate
                                .builder()
                                .type(SelectableValueTemplate.Type.DYNAMIC_STRING_OR_SEARCH_SELECT)
                                .selectablesSources(List.of(
                                        UrlBuilder.builder().urlTemplate("api/intern/arkiv/kodeverk/tilgangsrestriksjon").build()
                                ))
                                .build()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("skjermingshjemmel")
                                .displayName("Skjermingshjemmel")
                                .description("Henvisning til hjemmel (paragraf) i offentlighetsloven, sikkerhetsloven" +
                                        "\neller beskyttelsesinstruksen")
                                .build(),
                        SelectableValueTemplate
                                .builder()
                                .type(SelectableValueTemplate.Type.DYNAMIC_STRING_OR_SEARCH_SELECT)
                                .selectablesSources(List.of(
                                        UrlBuilder.builder().urlTemplate("api/intern/arkiv/kodeverk/skjermingshjemmel").build()
                                ))
                                .build()
                )
                .build();
    }

}
