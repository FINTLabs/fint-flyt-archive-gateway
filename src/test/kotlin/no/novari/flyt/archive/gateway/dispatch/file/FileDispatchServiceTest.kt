package no.novari.flyt.archive.gateway.dispatch.file

import io.netty.handler.timeout.ReadTimeoutException
import no.novari.fint.model.resource.Link
import no.novari.flyt.archive.gateway.dispatch.file.result.FileDispatchResult
import no.novari.flyt.archive.gateway.dispatch.model.File
import no.novari.flyt.archive.gateway.dispatch.model.instance.DokumentobjektDto
import no.novari.flyt.archive.gateway.dispatch.web.CreatedLocationPollTimeoutException
import no.novari.flyt.archive.gateway.dispatch.web.FintArchiveDispatchClient
import no.novari.flyt.archive.gateway.dispatch.web.flytfile.FlytFileClient
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
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
    fun givenSuccessFromGetFileAndSuccessFromPostFileShouldReturnAcceptedResult() {
        val fileMock = mockFile()
        whenever(flytFileClient.getFile(fileMock.fileId)).thenReturn(Mono.just(fileMock.file))
        whenever(fintArchiveDispatchClient.postFile(fileMock.file)).thenReturn(Mono.just(fileMock.archiveLink))

        StepVerifier
            .create(fileDispatchService.dispatch(fileMock.dokumentobjektDto))
            .expectNext(FileDispatchResult.accepted(fileMock.fileId, fileMock.archiveLink))
            .verifyComplete()
    }

    @Test
    fun givenErrorFromGetFileShouldReturnFailedCouldNotBeRetrievedResult() {
        val fileId = getUuid()
        whenever(flytFileClient.getFile(fileId)).thenReturn(Mono.error(RuntimeException()))

        StepVerifier
            .create(fileDispatchService.dispatch(DokumentobjektDto.builder().fileId(fileId).build()))
            .expectNext(FileDispatchResult.couldNotBeRetrieved(fileId))
            .verifyComplete()
    }

    @Test
    fun givenWebClientResponseExceptionFromPostFileShouldReturnDeclinedResult() {
        val fileMock = mockFile()
        val error: WebClientResponseException = mock()
        whenever(error.responseBodyAsString).thenReturn("test response body")
        whenever(flytFileClient.getFile(fileMock.fileId)).thenReturn(Mono.just(fileMock.file))
        whenever(fintArchiveDispatchClient.postFile(fileMock.file)).thenReturn(Mono.error(error))

        StepVerifier
            .create(fileDispatchService.dispatch(fileMock.dokumentobjektDto))
            .expectNext(FileDispatchResult.declined(fileMock.fileId, "test response body"))
            .verifyComplete()
    }

    @Test
    fun givenTimeoutFromDispatchClientShouldReturnTimedOutResult() {
        val fileMock = mockFile()
        whenever(flytFileClient.getFile(fileMock.fileId)).thenReturn(Mono.just(fileMock.file))
        whenever(fintArchiveDispatchClient.postFile(fileMock.file)).thenReturn(Mono.error(ReadTimeoutException()))

        StepVerifier
            .create(fileDispatchService.dispatch(fileMock.dokumentobjektDto))
            .expectNext(FileDispatchResult.timedOut(fileMock.fileId))
            .verifyComplete()
    }

    @Test
    fun givenCreatedLocationTimeoutFromDispatchClientShouldReturnTimedOutResult() {
        val fileMock = mockFile()
        whenever(flytFileClient.getFile(fileMock.fileId)).thenReturn(Mono.just(fileMock.file))
        whenever(
            fintArchiveDispatchClient.postFile(fileMock.file),
        ).thenReturn(Mono.error(CreatedLocationPollTimeoutException()))

        StepVerifier
            .create(fileDispatchService.dispatch(fileMock.dokumentobjektDto))
            .expectNext(FileDispatchResult.timedOut(fileMock.fileId))
            .verifyComplete()
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
