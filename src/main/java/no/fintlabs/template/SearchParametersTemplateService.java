package no.fintlabs.template;

import no.fintlabs.template.model.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchParametersTemplateService {

    ObjectTemplate createTemplate() {
        return ObjectTemplate
                .builder()
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("arkivdel")
                                .displayName("Arkivdel")
                                .description("")
                                .build(),
                        ValueTemplate
                                .builder()
                                .type(ValueTemplate.Type.BOOLEAN)
                                .build()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("administrativEnhet")
                                .displayName("Administrativ enhet")
                                .description("")
                                .build(),
                        ValueTemplate
                                .builder()
                                .type(ValueTemplate.Type.BOOLEAN)
                                .build()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("tilgangsrestriksjon")
                                .displayName("Tilgangsrestriksjon")
                                .description("")
                                .build(),
                        ValueTemplate
                                .builder()
                                .type(ValueTemplate.Type.BOOLEAN)
                                .build()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("saksmappetype")
                                .displayName("Saksmappetype")
                                .description("")
                                .build(),
                        ValueTemplate
                                .builder()
                                .type(ValueTemplate.Type.BOOLEAN)
                                .build()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("tittel")
                                .displayName("Tittel")
                                .description("")
                                .build(),
                        ValueTemplate
                                .builder()
                                .type(ValueTemplate.Type.BOOLEAN)
                                .build()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("klassering")
                                .displayName("Klassering")
                                .description("")
                                .build(),
                        ValueTemplate
                                .builder()
                                .type(ValueTemplate.Type.BOOLEAN)
                                .build()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("klasseringRekkefolge")
                                .displayName("Rekkefølge")
                                .description("")
                                .enableDependency(
                                        Dependency
                                                .builder()
                                                .hasAnyCombination(List.of(
                                                        List.of(ValuePredicate
                                                                .builder()
                                                                .key("klassering")
                                                                .defined(true)
                                                                .value("true")
                                                                .build()
                                                        )
                                                ))
                                                .build()
                                )
                                .build(),
                        ValueTemplate
                                .builder()
                                .type(ValueTemplate.Type.STRING)
                                .build()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("klasseringKlassifikasjonssystem")
                                .displayName("Klassifikasjonssystem")
                                .description("")
                                .enableDependency(
                                        Dependency
                                                .builder()
                                                .hasAnyCombination(List.of(
                                                        List.of(
                                                                ValuePredicate
                                                                        .builder()
                                                                        .key("klassering")
                                                                        .defined(true)
                                                                        .value("true")
                                                                        .build(),
                                                                ValuePredicate
                                                                        .builder()
                                                                        .key("klasseringRekkefolge")
                                                                        .defined(true)
                                                                        .notValue("")
                                                                        .build()
                                                        )
                                                ))
                                                .build()
                                )
                                .build(),
                        ValueTemplate
                                .builder()
                                .type(ValueTemplate.Type.BOOLEAN)
                                .build()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("klasseringKlasseId")
                                .displayName("KlasseID")
                                .description("")
                                .enableDependency(
                                        Dependency
                                                .builder()
                                                .hasAnyCombination(List.of(
                                                        List.of(
                                                                ValuePredicate
                                                                        .builder()
                                                                        .key("klassering")
                                                                        .defined(true)
                                                                        .value("true")
                                                                        .build(),
                                                                ValuePredicate
                                                                        .builder()
                                                                        .key("klasseringKlassifikasjonssystem")
                                                                        .defined(true)
                                                                        .notValue("")
                                                                        .build()
                                                        )
                                                ))
                                                .build()
                                )
                                .build(),
                        ValueTemplate
                                .builder()
                                .type(ValueTemplate.Type.BOOLEAN)
                                .build()
                )
                .build();
    }
}
