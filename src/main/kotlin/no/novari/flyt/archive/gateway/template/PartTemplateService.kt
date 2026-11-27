package no.novari.flyt.archive.gateway.template

import no.novari.fint.model.arkiv.kodeverk.PartRolle
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
    private val referenceTemplateFactory: ReferenceTemplateFactory,
) {
    fun createTemplate(): ObjectTemplate =
        ObjectTemplate
            .builder()
            .addTemplate(
                ElementConfig(
                    key = "partNavn",
                    displayName = "Navn",
                    description = "Navn på virksomhet eller person",
                ),
                ValueTemplate(
                    type = ValueTemplate.Type.DYNAMIC_STRING,
                ),
            ).addTemplate(
                ElementConfig(
                    key = "partRolle",
                    displayName = "Rolle",
                    description = "Partens rolle",
                ),
                SelectableValueTemplate(
                    type = SelectableValueTemplate.Type.DYNAMIC_STRING_OR_SEARCH_SELECT,
                    selectablesSources =
                        listOf(
                            UrlBuilder(
                                urlTemplate = "api/intern/arkiv/kodeverk/partrolle",
                            ),
                        ),
                    referenceTemplate = referenceTemplateFactory.create(PartRolle::class),
                ),
            ).addTemplate(
                ElementConfig(
                    key = "kontaktperson",
                    displayName = "Kontaktperson",
                    description = "Kontaktperson hos en organisasjon",
                ),
                ValueTemplate(
                    type = ValueTemplate.Type.DYNAMIC_STRING,
                ),
            ).addTemplate(
                ElementConfig(
                    key = "organisasjonsnummer",
                    displayName = "Organisasjonsnummer",
                    description = "Organisasjonsnummer",
                ),
                ValueTemplate(
                    type = ValueTemplate.Type.DYNAMIC_STRING,
                ),
            ).addTemplate(
                ElementConfig(
                    key = "fodselsnummer",
                    displayName = "Fødselsnummer",
                    description = "Fødselsnummer",
                ),
                ValueTemplate(
                    type = ValueTemplate.Type.DYNAMIC_STRING,
                ),
            ).addTemplate(
                ElementConfig(
                    key = "adresse",
                    displayName = "Adresse",
                    description = "Adresse",
                ),
                adresseTemplateService.createTemplate(),
            ).addTemplate(
                ElementConfig(
                    key = "kontaktinformasjon",
                    displayName = "Kontaktinformasjon",
                    description = "Kontaktinformasjon",
                ),
                kontaktinformasjonTemplateService.createTemplate(),
            ).build()
}
