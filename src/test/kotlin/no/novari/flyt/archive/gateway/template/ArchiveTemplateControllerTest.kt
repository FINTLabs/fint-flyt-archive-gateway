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
    fun `getTemplate returns the archive template`() {
        val template =
            MappingTemplate
                .builder()
                .displayName("Arkivering")
                .rootObjectTemplate(ObjectTemplate.builder().build())
                .build()
        whenever(archiveTemplateService.createTemplate()).thenReturn(template)

        val response = archiveTemplateController.getTemplate()

        assertThat(response).isEqualTo(template)

        verify(archiveTemplateService).createTemplate()
        verifyNoMoreInteractions(archiveTemplateService)
    }
}
