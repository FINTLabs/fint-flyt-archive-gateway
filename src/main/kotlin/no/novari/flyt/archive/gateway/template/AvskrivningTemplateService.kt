package no.novari.flyt.archive.gateway.template

import no.novari.flyt.archive.gateway.template.model.ElementConfig
import no.novari.flyt.archive.gateway.template.model.ObjectTemplate
import no.novari.flyt.archive.gateway.template.model.Selectable
import no.novari.flyt.archive.gateway.template.model.SelectableValueTemplate
import no.novari.flyt.archive.gateway.template.model.ValueTemplate
import org.springframework.stereotype.Service

@Service
class AvskrivningTemplateService {
    fun createTemplate(): ObjectTemplate =
        ObjectTemplate
            .builder()
            .addTemplate(
                ElementConfig
                    .builder()
                    .key("avskrivningsdato")
                    .displayName("Avskrivningsdato")
                    .description(
                        "Dato og klokkeslett for avskrivning. Må være på ISO 8601-format YYYY-MM-DDThh:mm:ssZ " +
                            "(UTC), for eksempel 2026-08-24T09:12:48Z. Hvis feltet er tomt og avskrivningsmåte " +
                            "er fylt ut, brukes dagens dato og klokkeslett ved utsending til arkivet.",
                    ).build(),
                ValueTemplate.builder().type(ValueTemplate.Type.DYNAMIC_STRING).build(),
            ).addTemplate(
                ElementConfig
                    .builder()
                    .key("avskrivningsmate")
                    .displayName("Avskrivningsmåte")
                    .description(
                        "Hvordan journalposten avskrives. Kun aktuelt når journalposttype er I (Inngående brev).",
                    ).build(),
                SelectableValueTemplate
                    .builder()
                    .type(SelectableValueTemplate.Type.DYNAMIC_STRING_OR_SEARCH_SELECT)
                    .selectables(
                        listOf(
                            Selectable(displayName = "[BU] Besvart med brev", value = "BU"),
                            Selectable(displayName = "[BE] Besvart med e-post", value = "BE"),
                            Selectable(displayName = "[TLF] Besvart på telefon", value = "TLF"),
                            Selectable(displayName = "[TE] Tatt til etterretning", value = "TE"),
                            Selectable(displayName = "[TO] Tatt til orientering", value = "TO"),
                        ),
                    ).build(),
            ).build()
}
