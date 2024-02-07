package no.fintlabs.flyt.gateway.application.archive.template;

import no.fintlabs.flyt.gateway.application.archive.template.model.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PartTemplateService {

    private final AdresseTemplateService adresseTemplateService;
    private final KontaktinformasjonTemplateService kontaktinformasjonTemplateService;

    public PartTemplateService(
            AdresseTemplateService adresseTemplateService,
            KontaktinformasjonTemplateService kontaktinformasjonTemplateService
    ) {
        this.adresseTemplateService = adresseTemplateService;
        this.kontaktinformasjonTemplateService = kontaktinformasjonTemplateService;
    }

    public ObjectTemplate createTemplate() {
        return ObjectTemplate
                .builder()
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("partNavn")
                                .displayName("Navn")
                                .description("Navn på virksomhet eller person")
                                .build(),
                        ValueTemplate
                                .builder()
                                .type(ValueTemplate.Type.DYNAMIC_STRING)
                                .build()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("partRolle")
                                .displayName("Rolle")
                                .description("Partens rolle")
                                .build(),
                        SelectableValueTemplate
                                .builder()
                                .type(SelectableValueTemplate.Type.DYNAMIC_STRING_OR_SEARCH_SELECT)
                                .selectablesSources(List.of(
                                        UrlBuilder.builder().urlTemplate("api/intern/arkiv/kodeverk/partrolle").build()
                                ))
                                .build()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("kontaktperson")
                                .displayName("Kontaktperson")
                                .description("Kontaktperson hos en organisasjon")
                                .build(),
                        ValueTemplate
                                .builder()
                                .type(ValueTemplate.Type.DYNAMIC_STRING)
                                .build()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("adresse")
                                .displayName("Adresse")
                                .description("Adresse")
                                .build(),
                        adresseTemplateService.createTemplate()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("kontaktinformasjon")
                                .displayName("Kontaktinformasjon")
                                .description("Kontaktinformasjon")
                                .build(),
                        kontaktinformasjonTemplateService.createTemplate()
                )
                .build();
    }
}
