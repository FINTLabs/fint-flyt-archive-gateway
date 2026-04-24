package no.novari.flyt.archive.gateway.template

import no.novari.flyt.archive.gateway.template.model.ElementConfig
import no.novari.flyt.archive.gateway.template.model.ObjectTemplate
import no.novari.flyt.archive.gateway.template.model.SelectableValueTemplate
import no.novari.flyt.archive.gateway.template.model.UrlBuilder
import no.novari.flyt.archive.gateway.template.model.ValueTemplate
import org.springframework.stereotype.Service

@Service
class PartTemplateService(
    private val adresseTemplateService: AdresseTemplateService,
    private val kontaktinformasjonTemplateService: KontaktinformasjonTemplateService,
) {
    fun createTemplate(): ObjectTemplate =
        ObjectTemplate
            .builder()
            .addTemplate(
                ElementConfig
                    .builder()
                    .key(
                        "partNavn",
                    ).displayName("Navn")
                    .description("Navn på virksomhet eller person")
                    .build(),
                ValueTemplate.builder().type(ValueTemplate.Type.DYNAMIC_STRING).build(),
            ).addTemplate(
                ElementConfig
                    .builder()
                    .key("partRolle")
                    .displayName("Rolle")
                    .description("Partens rolle")
                    .build(),
                SelectableValueTemplate
                    .builder()
                    .type(SelectableValueTemplate.Type.DYNAMIC_STRING_OR_SEARCH_SELECT)
                    .selectablesSources(
                        listOf(UrlBuilder.builder().urlTemplate("api/intern/arkiv/kodeverk/partrolle").build()),
                    ).build(),
            ).addTemplate(
                ElementConfig
                    .builder()
                    .key(
                        "kontaktperson",
                    ).displayName("Kontaktperson")
                    .description("Kontaktperson hos en organisasjon")
                    .build(),
                ValueTemplate.builder().type(ValueTemplate.Type.DYNAMIC_STRING).build(),
            ).addTemplate(
                ElementConfig
                    .builder()
                    .key("organisasjonsnummer")
                    .displayName("Organisasjonsnummer")
                    .description("Organisasjonsnummer")
                    .build(),
                ValueTemplate.builder().type(ValueTemplate.Type.DYNAMIC_STRING).build(),
            ).addTemplate(
                ElementConfig
                    .builder()
                    .key(
                        "fodselsnummer",
                    ).displayName("Fødselsnummer")
                    .description("Fødselsnummer")
                    .build(),
                ValueTemplate.builder().type(ValueTemplate.Type.DYNAMIC_STRING).build(),
            ).addTemplate(
                ElementConfig
                    .builder()
                    .key("adresse")
                    .displayName("Adresse")
                    .description("Adresse")
                    .build(),
                adresseTemplateService.createTemplate(),
            ).addTemplate(
                ElementConfig
                    .builder()
                    .key(
                        "kontaktinformasjon",
                    ).displayName("Kontaktinformasjon")
                    .description("Kontaktinformasjon")
                    .build(),
                kontaktinformasjonTemplateService.createTemplate(),
            ).build()
}
