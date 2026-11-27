package no.novari.flyt.archive.gateway.template

import no.novari.fint.model.arkiv.noark.Klassifikasjonssystem
import no.novari.flyt.archive.gateway.template.model.ElementConfig
import no.novari.flyt.archive.gateway.template.model.ObjectTemplate
import no.novari.flyt.archive.gateway.template.model.SelectableValueTemplate
import no.novari.flyt.archive.gateway.template.model.UrlBuilder
import no.novari.flyt.archive.gateway.template.model.ValueTemplate
import org.springframework.stereotype.Service

@Service
class KlasseringTemplateService(
    private val skjermingTemplateService: SkjermingTemplateService,
    private val referenceTemplateFactory: ReferenceTemplateFactory,
) {
    fun createTemplate(): ObjectTemplate =
        ObjectTemplate
            .builder()
            .addTemplate(
                ElementConfig(
                    key = "rekkefolge",
                    displayName = "Rekkefølge",
                    description =
                        """
                        Rekkefølge for klassifiseringer. 
                        Ved bruk av primær, sekundær og tertiærklasseringer, bruk følgende verdier:
                        1 for primær, 2 for sekundær, og 3 for tertiær.
                        """.trimIndent(),
                ),
                ValueTemplate(
                    type = ValueTemplate.Type.STRING,
                ),
            ).addTemplate(
                ElementConfig(
                    key = "klassifikasjonssystem",
                    displayName = "Klassifikasjonssystem",
                    description = "Beskriver den overordnede strukturen for mappene i en eller flere arkivdeler",
                ),
                SelectableValueTemplate(
                    type = SelectableValueTemplate.Type.DYNAMIC_STRING_OR_SEARCH_SELECT,
                    selectablesSources =
                        listOf(
                            UrlBuilder(
                                urlTemplate = "api/intern/arkiv/kodeverk/klassifikasjonssystem",
                            ),
                        ),
                    referenceTemplate = referenceTemplateFactory.create(Klassifikasjonssystem::class),
                ),
            ).addTemplate(
                ElementConfig(
                    key = "klasseId",
                    displayName = "KlasseID",
                    description = "Entydig identifikasjon av klassen innenfor klassifikasjonssystemet",
                ),
                SelectableValueTemplate(
                    type = SelectableValueTemplate.Type.DYNAMIC_STRING_OR_SEARCH_SELECT,
                    selectablesSources =
                        listOf(
                            UrlBuilder(
                                urlTemplate = "api/intern/arkiv/kodeverk/klasse",
                                valueRefPerRequestParamKey =
                                    mapOf("klassifikasjonssystemLink" to "klassifikasjonssystem"),
                            ),
                        ),
                ),
            ).addTemplate(
                ElementConfig(
                    key = "tittel",
                    displayName = "Tittel",
                    description = "Tittel eller navn på arkivenheten",
                ),
                ValueTemplate(
                    type = ValueTemplate.Type.DYNAMIC_STRING,
                ),
            ).addTemplate(
                ElementConfig(
                    key = "skjerming",
                    displayName = "Skjerming",
                    description = "Skjerming av klasse",
                ),
                skjermingTemplateService.createTemplate(),
            ).build()
}
