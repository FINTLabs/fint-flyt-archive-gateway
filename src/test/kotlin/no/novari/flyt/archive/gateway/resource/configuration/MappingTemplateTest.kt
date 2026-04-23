package no.novari.flyt.archive.gateway.resource.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.validation.Validation.buildDefaultValidatorFactory
import jakarta.validation.Validator
import no.novari.flyt.archive.gateway.template.AdresseTemplateService
import no.novari.flyt.archive.gateway.template.ArchiveTemplateService
import no.novari.flyt.archive.gateway.template.DokumentbeskrivelseTemplateService
import no.novari.flyt.archive.gateway.template.DokumentobjektTemplateService
import no.novari.flyt.archive.gateway.template.JournalpostTemplateService
import no.novari.flyt.archive.gateway.template.KlasseringTemplateService
import no.novari.flyt.archive.gateway.template.KontaktinformasjonTemplateService
import no.novari.flyt.archive.gateway.template.KorrespondansepartTemplateService
import no.novari.flyt.archive.gateway.template.PartTemplateService
import no.novari.flyt.archive.gateway.template.SakTemplateService
import no.novari.flyt.archive.gateway.template.SearchParametersTemplateService
import no.novari.flyt.archive.gateway.template.SkjermingTemplateService
import no.novari.flyt.archive.gateway.template.model.MappingTemplate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
    classes = [
        AdresseTemplateService::class,
        ArchiveTemplateService::class,
        SearchParametersTemplateService::class,
        DokumentbeskrivelseTemplateService::class,
        DokumentobjektTemplateService::class,
        JournalpostTemplateService::class,
        KlasseringTemplateService::class,
        KontaktinformasjonTemplateService::class,
        KorrespondansepartTemplateService::class,
        SakTemplateService::class,
        SkjermingTemplateService::class,
        PartTemplateService::class,
        ObjectMapper::class,
    ],
)
class MappingTemplateTest {
    @Autowired
    lateinit var archiveTemplateService: ArchiveTemplateService

    @Autowired
    lateinit var objectMapper: ObjectMapper

    private lateinit var validator: Validator

    @BeforeEach
    fun setup() {
        validator = buildDefaultValidatorFactory().validator
    }

    @Test
    @Disabled
    fun shouldCreateTemplate() {
        val template = archiveTemplateService.createTemplate()
        validator.validate(template)
        objectMapper.writeValueAsString(template)
        assertEquals(readTemplate(), template)
    }

    private fun readTemplate(): MappingTemplate? = null
}
