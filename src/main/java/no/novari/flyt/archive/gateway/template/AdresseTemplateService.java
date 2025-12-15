package no.novari.flyt.archive.gateway.template;

import no.novari.flyt.archive.gateway.template.model.ElementConfig;
import no.novari.flyt.archive.gateway.template.model.ObjectTemplate;
import no.novari.flyt.archive.gateway.template.model.ValueTemplate;
import org.springframework.stereotype.Service;

@Service
public class AdresseTemplateService {

    public ObjectTemplate createTemplate() {
        return ObjectTemplate
                .builder()
                .addCollectionTemplate(
                        ElementConfig
                                .builder()
                                .key("adresselinje")
                                .displayName("Adresselinjer")
                                .description("Adresseinformasjon. Linjer representeres hver for seg, fra øverst til " +
                                        "nederst. \nDette kan være: Gateadresse, Postboksadresse, Bolignummer, " +
                                        "C/O adresse, Attn, Mottak på vegne av andre.")
                                .build(),
                        ValueTemplate
                                .builder()
                                .type(ValueTemplate.Type.DYNAMIC_STRING)
                                .build()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("postnummer")
                                .displayName("Postnummer")
                                .description("Postnummer")
                                .build(),
                        ValueTemplate
                                .builder()
                                .type(ValueTemplate.Type.DYNAMIC_STRING)
                                .build()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("poststed")
                                .displayName("Poststed")
                                .description("Poststed")
                                .build(),
                        ValueTemplate
                                .builder()
                                .type(ValueTemplate.Type.DYNAMIC_STRING)
                                .build()
                )
                .build();
    }

}
