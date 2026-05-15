package no.novari.flyt.archive.gateway.template

import no.novari.flyt.archive.gateway.template.model.MappingTemplate
import no.novari.flyt.archive.gateway.template.model.ObjectTemplate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus

@ExtendWith(MockitoExtension::class)
class ArchiveTemplateControllerTest {
    @Mock
    lateinit var archiveTemplateService: ArchiveTemplateService

    private lateinit var archiveTemplateController: ArchiveTemplateController

    @BeforeEach
    fun setUp() {
        archiveTemplateController = ArchiveTemplateController(archiveTemplateService)
    }

    @Test
    fun getTemplateShouldReturn200WithArchiveTemplate() {
        val template =
            MappingTemplate
                .builder()
                .displayName("Arkivering")
                .rootObjectTemplate(ObjectTemplate.builder().build())
                .build()
        whenever(archiveTemplateService.createTemplate()).thenReturn(template)

        val response = archiveTemplateController.getTemplate()

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isEqualTo(template)

        verify(archiveTemplateService).createTemplate()
        verifyNoMoreInteractions(archiveTemplateService)
    }
}
