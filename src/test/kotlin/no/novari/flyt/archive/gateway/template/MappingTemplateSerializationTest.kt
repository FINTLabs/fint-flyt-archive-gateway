package no.novari.flyt.archive.gateway.template

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Test

class MappingTemplateSerializationTest {
    private val objectMapper = jacksonObjectMapper()

    private val archiveTemplateService =
        ArchiveTemplateService(
            SearchParametersTemplateService(),
            SakTemplateService(
                KlasseringTemplateService(SkjermingTemplateService()),
                SkjermingTemplateService(),
                JournalpostTemplateService(
                    KorrespondansepartTemplateService(
                        AdresseTemplateService(),
                        KontaktinformasjonTemplateService(),
                        SkjermingTemplateService(),
                    ),
                    DokumentbeskrivelseTemplateService(
                        DokumentobjektTemplateService(),
                        SkjermingTemplateService(),
                    ),
                    SkjermingTemplateService(),
                ),
                PartTemplateService(
                    AdresseTemplateService(),
                    KontaktinformasjonTemplateService(),
                ),
            ),
            JournalpostTemplateService(
                KorrespondansepartTemplateService(
                    AdresseTemplateService(),
                    KontaktinformasjonTemplateService(),
                    SkjermingTemplateService(),
                ),
                DokumentbeskrivelseTemplateService(
                    DokumentobjektTemplateService(),
                    SkjermingTemplateService(),
                ),
                SkjermingTemplateService(),
            ),
        )

    @Test
    fun `the created template is serializable`() {
        val template = archiveTemplateService.createTemplate()

        assertThatCode { objectMapper.writeValueAsString(template) }.doesNotThrowAnyException()
    }
}
