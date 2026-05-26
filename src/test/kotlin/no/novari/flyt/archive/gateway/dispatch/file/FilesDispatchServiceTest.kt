package no.novari.flyt.archive.gateway.dispatch.file

import no.novari.fint.model.resource.Link
import no.novari.flyt.archive.gateway.dispatch.file.result.FileDispatchResult
import no.novari.flyt.archive.gateway.dispatch.file.result.FilesDispatchResult
import no.novari.flyt.archive.gateway.dispatch.model.instance.DokumentobjektDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class FilesDispatchServiceTest {
    @Mock
    private lateinit var fileDispatchService: FileDispatchService

    @InjectMocks
    private lateinit var filesDispatchService: FilesDispatchService

    @Test
    fun `given no files, returns an accepted empty result`() {
        val result = filesDispatchService.dispatch(emptyList())

        assertThat(result).isEqualTo(FilesDispatchResult.accepted(emptyMap()))
    }

    @Test
    fun `given accepted files, returns an accepted result`() {
        val fileId = UUID.randomUUID()
        val dokumentobjektDto = DokumentobjektDto.builder().fileId(fileId).build()
        val link = Link.with("link")
        whenever(fileDispatchService.dispatch(dokumentobjektDto))
            .thenReturn(FileDispatchResult.accepted(fileId, link))

        val result = filesDispatchService.dispatch(listOf(dokumentobjektDto))

        assertThat(result).isEqualTo(FilesDispatchResult.accepted(mapOf(fileId to link)))
    }

    @Test
    fun `given a declined file, returns a declined result`() {
        val dokumentobjektDto = DokumentobjektDto.builder().fileId(UUID.randomUUID()).build()
        whenever(fileDispatchService.dispatch(dokumentobjektDto))
            .thenReturn(FileDispatchResult.declined(dokumentobjektDto.fileId!!, "test error message"))

        val result = filesDispatchService.dispatch(listOf(dokumentobjektDto))

        assertThat(result).isEqualTo(FilesDispatchResult.declined("test error message"))
    }
}
