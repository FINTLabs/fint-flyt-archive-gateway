package no.novari.flyt.archive.gateway.template;

import no.novari.flyt.archive.gateway.template.model.ElementConfig;
import no.novari.flyt.archive.gateway.template.model.ObjectTemplate;
import no.novari.flyt.archive.gateway.template.model.SelectableValueTemplate;
import no.novari.flyt.archive.gateway.template.model.UrlBuilder;
import no.novari.flyt.archive.gateway.template.model.ValueTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DokumentobjektTemplateService {

    public ObjectTemplate createTemplate() {
        return ObjectTemplate
                .builder()
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("variantformat")
                                .displayName("Variantformat")
                                .description("Angivelse av hvilken variant et dokument forekommer i")
                                .build(),
                        SelectableValueTemplate
                                .builder()
                                .type(SelectableValueTemplate.Type.DYNAMIC_STRING_OR_SEARCH_SELECT)
                                .selectablesSources(List.of(
                                        UrlBuilder
                                                .builder()
                                                .urlTemplate("api/intern/arkiv/kodeverk/variantformat")
                                                .build()
                                ))
                                .build()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("filformat")
                                .displayName("Filformat")
                                .description("Dokumentets format")
                                .build(),
                        SelectableValueTemplate
                                .builder()
                                .type(SelectableValueTemplate.Type.DYNAMIC_STRING_OR_SEARCH_SELECT)
                                .selectablesSources(List.of(
                                        UrlBuilder
                                                .builder()
                                                .urlTemplate("api/intern/arkiv/kodeverk/format")
                                                .build()
                                ))
                                .build()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("fil")
                                .displayName("Fil")
                                .description("Referanse til filen som inneholder det elektroniske dokumentet " +
                                        "som dokumentobjektet beskriver")
                                .build(),
                        ValueTemplate
                                .builder()
                                .type(ValueTemplate.Type.FILE)
                                .build()
                )
                .build();
    }
}
