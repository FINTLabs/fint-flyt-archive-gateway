package no.novari.flyt.archive.gateway.dispatch.file

import no.novari.fint.model.resource.Link
import no.novari.flyt.archive.gateway.dispatch.file.result.FileDispatchResult
import no.novari.flyt.archive.gateway.dispatch.file.result.FilesDispatchResult
import no.novari.flyt.archive.gateway.dispatch.model.instance.DokumentobjektDto
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class FilesDispatchServiceTest {
    @Mock
    private lateinit var fileDispatchService: FileDispatchService

    @InjectMocks
    private lateinit var filesDispatchService: FilesDispatchService

    @Test
    fun givenNoFilesShouldReturnAcceptedEmptyResult() {
        StepVerifier
            .create(filesDispatchService.dispatch(emptyList()))
            .expectNext(FilesDispatchResult.accepted(emptyMap()))
            .verifyComplete()
    }

    @Test
    fun givenAcceptedFilesShouldReturnAcceptedResult() {
        val fileId = UUID.randomUUID()
        val dokumentobjektDto = DokumentobjektDto.builder().fileId(fileId).build()
        val link = Link.with("link")
        whenever(
            fileDispatchService.dispatch(dokumentobjektDto),
        ).thenReturn(Mono.just(FileDispatchResult.accepted(fileId, link)))

        StepVerifier
            .create(filesDispatchService.dispatch(listOf(dokumentobjektDto)))
            .expectNext(FilesDispatchResult.accepted(mapOf(fileId to link)))
            .verifyComplete()
    }

    @Test
    fun givenDeclinedFileShouldReturnDeclinedResult() {
        val dokumentobjektDto = DokumentobjektDto.builder().fileId(UUID.randomUUID()).build()
        whenever(fileDispatchService.dispatch(dokumentobjektDto))
            .thenReturn(Mono.just(FileDispatchResult.declined(dokumentobjektDto.fileId!!, "test error message")))

        StepVerifier
            .create(filesDispatchService.dispatch(listOf(dokumentobjektDto)))
            .expectNext(FilesDispatchResult.declined("test error message"))
            .verifyComplete()
    }
}
