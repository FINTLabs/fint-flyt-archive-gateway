package no.novari.flyt.archive.gateway.template

import no.novari.flyt.archive.gateway.template.model.Dependency
import no.novari.flyt.archive.gateway.template.model.ElementConfig
import no.novari.flyt.archive.gateway.template.model.ObjectTemplate
import no.novari.flyt.archive.gateway.template.model.ValuePredicate
import no.novari.flyt.archive.gateway.template.model.ValueTemplate
import org.springframework.stereotype.Service

@Service
class SearchParametersTemplateService {
    fun createTemplate(): ObjectTemplate =
        ObjectTemplate
            .builder()
            .addTemplate(
                ElementConfig(
                    key = "arkivdel",
                    displayName = "Arkivdel",
                    description = "",
                ),
                ValueTemplate(
                    type = ValueTemplate.Type.BOOLEAN,
                ),
            ).addTemplate(
                ElementConfig(
                    key = "administrativEnhet",
                    displayName = "Administrativ enhet",
                    description = "",
                ),
                ValueTemplate(
                    type = ValueTemplate.Type.BOOLEAN,
                ),
            ).addTemplate(
                ElementConfig(
                    key = "tilgangsrestriksjon",
                    displayName = "Tilgangsrestriksjon",
                    description = "",
                ),
                ValueTemplate(
                    type = ValueTemplate.Type.BOOLEAN,
                ),
            ).addTemplate(
                ElementConfig(
                    key = "saksmappetype",
                    displayName = "Saksmappetype",
                    description = "",
                ),
                ValueTemplate(
                    type = ValueTemplate.Type.BOOLEAN,
                ),
            ).addTemplate(
                ElementConfig(
                    key = "saksstatus",
                    displayName = "Saksstatus",
                    description = "",
                ),
                ValueTemplate(
                    type = ValueTemplate.Type.BOOLEAN,
                ),
            ).addTemplate(
                ElementConfig(
                    key = "tittel",
                    displayName = "Tittel",
                    description = "",
                ),
                ValueTemplate(
                    type = ValueTemplate.Type.BOOLEAN,
                ),
            ).addTemplate(
                ElementConfig(
                    key = "klassering",
                    displayName = "Klassering",
                    description = "",
                ),
                ValueTemplate(
                    type = ValueTemplate.Type.BOOLEAN,
                ),
            ).addTemplate(
                ElementConfig(
                    key = "klasseringRekkefolge",
                    displayName = "Rekkefølge",
                    description = "",
                    enableDependency =
                        Dependency(
                            hasAnyCombination =
                                listOf(
                                    listOf(
                                        ValuePredicate(
                                            key = "klassering",
                                            defined = true,
                                            value = "true",
                                        ),
                                    ),
                                ),
                        ),
                ),
                ValueTemplate(
                    type = ValueTemplate.Type.STRING,
                ),
            ).addTemplate(
                ElementConfig(
                    key = "klasseringKlassifikasjonssystem",
                    displayName = "Klassifikasjonssystem",
                    description = "",
                    enableDependency =
                        Dependency(
                            hasAnyCombination =
                                listOf(
                                    listOf(
                                        ValuePredicate(
                                            key = "klassering",
                                            defined = true,
                                            value = "true",
                                        ),
                                        ValuePredicate(
                                            key = "klasseringRekkefolge",
                                            defined = true,
                                            notValue = "",
                                        ),
                                    ),
                                ),
                        ),
                ),
                ValueTemplate(
                    type = ValueTemplate.Type.BOOLEAN,
                ),
            ).addTemplate(
                ElementConfig(
                    key = "klasseringKlasseId",
                    displayName = "KlasseID",
                    description = "",
                    enableDependency =
                        Dependency(
                            hasAnyCombination =
                                listOf(
                                    listOf(
                                        ValuePredicate(
                                            key = "klassering",
                                            defined = true,
                                            value = "true",
                                        ),
                                        ValuePredicate(
                                            key = "klasseringKlassifikasjonssystem",
                                            defined = true,
                                            notValue = "",
                                        ),
                                    ),
                                ),
                        ),
                ),
                ValueTemplate(
                    type = ValueTemplate.Type.BOOLEAN,
                ),
            ).build()
}
