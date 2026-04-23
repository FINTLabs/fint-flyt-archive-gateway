package no.novari.flyt.archive.gateway.template

import no.novari.flyt.archive.gateway.template.model.ElementConfig
import no.novari.flyt.archive.gateway.template.model.ObjectTemplate
import no.novari.flyt.archive.gateway.template.model.SelectableValueTemplate
import no.novari.flyt.archive.gateway.template.model.UrlBuilder
import no.novari.flyt.archive.gateway.template.model.ValueTemplate
import org.springframework.stereotype.Service

@Service
class KorrespondansepartTemplateService(
    private val adresseTemplateService: AdresseTemplateService,
    private val kontaktinformasjonTemplateService: KontaktinformasjonTemplateService,
    private val skjermingTemplateService: SkjermingTemplateService,
) {
    fun createTemplate(): ObjectTemplate =
        ObjectTemplate
            .builder()
            .addTemplate(
                ElementConfig
                    .builder()
                    .key("korrespondanseparttype")
                    .displayName("Korrespondanseparttype")
                    .description("Type korrespondansepart")
                    .build(),
                SelectableValueTemplate
                    .builder()
                    .type(SelectableValueTemplate.Type.DYNAMIC_STRING_OR_SEARCH_SELECT)
                    .selectablesSources(
                        listOf(
                            UrlBuilder
                                .builder()
                                .urlTemplate(
                                    "api/intern/arkiv/kodeverk/korrespondanseparttype",
                                ).build(),
                        ),
                    ).build(),
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
                    .key(
                        "korrespondansepartNavn",
                    ).displayName("Navn")
                    .description("Navn på person eller organisasjon")
                    .build(),
                ValueTemplate.builder().type(ValueTemplate.Type.DYNAMIC_STRING).build(),
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
            ).addTemplate(
                ElementConfig
                    .builder()
                    .key(
                        "skjerming",
                    ).displayName("Skjerming")
                    .description("Skjerming av korrespodansepart")
                    .build(),
                skjermingTemplateService.createTemplate(),
            ).build()
}
