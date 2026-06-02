package no.novari.flyt.archive.gateway.dispatch.file

import no.novari.fint.model.resource.Link
import no.novari.flyt.archive.gateway.dispatch.file.result.FileDispatchResult
import no.novari.flyt.archive.gateway.dispatch.model.File
import no.novari.flyt.archive.gateway.dispatch.model.instance.DokumentobjektDto
import no.novari.flyt.archive.gateway.dispatch.web.CreatedLocationPollTimeoutException
import no.novari.flyt.archive.gateway.dispatch.web.FintArchiveDispatchClient
import no.novari.flyt.archive.gateway.dispatch.web.flytfile.FlytFileClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.ResourceAccessException
import java.net.http.HttpTimeoutException
import java.util.Random
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class FileDispatchServiceTest {
    @Mock
    private lateinit var fintArchiveDispatchClient: FintArchiveDispatchClient

    @Mock
    private lateinit var flytFileClient: FlytFileClient

    @InjectMocks
    private lateinit var fileDispatchService: FileDispatchService

    private lateinit var random: Random

    @BeforeEach
    fun setup() {
        random = Random(42)
    }

    @Test
    fun `given a successful getFile and a successful postFile, returns an accepted result`() {
        val fileMock = mockFile()
        whenever(flytFileClient.getFile(fileMock.fileId)).thenReturn(fileMock.file)
        whenever(fintArchiveDispatchClient.postFile(fileMock.file)).thenReturn(fileMock.archiveLink)

        val result = fileDispatchService.dispatch(fileMock.dokumentobjektDto)

        assertThat(result).isEqualTo(FileDispatchResult.accepted(fileMock.fileId, fileMock.archiveLink))
    }

    @Test
    fun `given an error from getFile, returns a couldNotBeRetrieved result`() {
        val fileId = getUuid()
        whenever(flytFileClient.getFile(fileId)).thenThrow(RuntimeException())

        val result = fileDispatchService.dispatch(DokumentobjektDto.builder().fileId(fileId).build())

        assertThat(result).isEqualTo(FileDispatchResult.couldNotBeRetrieved(fileId))
    }

    @Test
    fun `given a RestClientResponseException from postFile, returns a declined result`() {
        val fileMock = mockFile()
        val error: HttpClientErrorException = mock()
        whenever(error.responseBodyAsString).thenReturn("test response body")
        whenever(flytFileClient.getFile(fileMock.fileId)).thenReturn(fileMock.file)
        whenever(fintArchiveDispatchClient.postFile(fileMock.file)).thenThrow(error)

        val result = fileDispatchService.dispatch(fileMock.dokumentobjektDto)

        assertThat(result).isEqualTo(FileDispatchResult.declined(fileMock.fileId, "test response body"))
    }

    @Test
    fun `given a read timeout from the dispatch client, returns a timed-out result`() {
        val fileMock = mockFile()
        whenever(flytFileClient.getFile(fileMock.fileId)).thenReturn(fileMock.file)
        whenever(fintArchiveDispatchClient.postFile(fileMock.file))
            .thenThrow(ResourceAccessException("read timeout", HttpTimeoutException("read timeout")))

        val result = fileDispatchService.dispatch(fileMock.dokumentobjektDto)

        assertThat(result).isEqualTo(FileDispatchResult.timedOut(fileMock.fileId))
    }

    @Test
    fun `given a CreatedLocationPollTimeoutException from the dispatch client, returns a timed-out result`() {
        val fileMock = mockFile()
        whenever(flytFileClient.getFile(fileMock.fileId)).thenReturn(fileMock.file)
        whenever(fintArchiveDispatchClient.postFile(fileMock.file))
            .thenThrow(CreatedLocationPollTimeoutException())

        val result = fileDispatchService.dispatch(fileMock.dokumentobjektDto)

        assertThat(result).isEqualTo(FileDispatchResult.timedOut(fileMock.fileId))
    }

    private fun mockFile(): FileMock {
        val fileId = getUuid()
        return FileMock(
            fileId = fileId,
            file = mock(),
            archiveLink = mock(),
            dokumentobjektDto = DokumentobjektDto.builder().fileId(fileId).build(),
        )
    }

    private fun getUuid(): UUID {
        val bytes = ByteArray(7)
        random.nextBytes(bytes)
        return UUID.nameUUIDFromBytes(bytes)
    }

    private data class FileMock(
        val fileId: UUID,
        val file: File,
        val archiveLink: Link,
        val dokumentobjektDto: DokumentobjektDto,
    )
}
