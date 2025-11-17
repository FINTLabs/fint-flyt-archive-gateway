package no.novari.flyt.archive.gateway.resource.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import no.novari.flyt.archive.gateway.template.AdresseTemplateService;
import no.novari.flyt.archive.gateway.template.ArchiveTemplateService;
import no.novari.flyt.archive.gateway.template.DokumentbeskrivelseTemplateService;
import no.novari.flyt.archive.gateway.template.DokumentobjektTemplateService;
import no.novari.flyt.archive.gateway.template.JournalpostTemplateService;
import no.novari.flyt.archive.gateway.template.KlasseringTemplateService;
import no.novari.flyt.archive.gateway.template.KontaktinformasjonTemplateService;
import no.novari.flyt.archive.gateway.template.KorrespondansepartTemplateService;
import no.novari.flyt.archive.gateway.template.PartTemplateService;
import no.novari.flyt.archive.gateway.template.SakTemplateService;
import no.novari.flyt.archive.gateway.template.SearchParametersTemplateService;
import no.novari.flyt.archive.gateway.template.SkjermingTemplateService;
import no.novari.flyt.archive.gateway.template.model.MappingTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Set;

import static jakarta.validation.Validation.buildDefaultValidatorFactory;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        AdresseTemplateService.class,
        ArchiveTemplateService.class,
        SearchParametersTemplateService.class,
        DokumentbeskrivelseTemplateService.class,
        DokumentobjektTemplateService.class,
        JournalpostTemplateService.class,
        KlasseringTemplateService.class,
        KontaktinformasjonTemplateService.class,
        KorrespondansepartTemplateService.class,
        SakTemplateService.class,
        SkjermingTemplateService.class,
        PartTemplateService.class,
        ObjectMapper.class
})
class MappingTemplateTest {

    @Autowired
    ArchiveTemplateService archiveTemplateService;

    @Autowired
    ObjectMapper objectMapper;

    Validator validator;

    @BeforeEach
    void setup() {
        ValidatorFactory factory = buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @Disabled
    void shouldCreateTemplate() throws Exception {
        MappingTemplate template = archiveTemplateService.createTemplate();
        Set<ConstraintViolation<MappingTemplate>> constraintViolations = validator.validate(template);
        String templateJson = objectMapper.writeValueAsString(template);
        assertEquals(readTemplate(), template);
    }

    // add this method or replace with your method to read the template
    MappingTemplate readTemplate() {
        // your logic to read the template
        return null;
    }

}
