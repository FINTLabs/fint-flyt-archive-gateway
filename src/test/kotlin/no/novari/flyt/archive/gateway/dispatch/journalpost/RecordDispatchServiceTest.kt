package no.novari.flyt.archive.gateway.dispatch.journalpost

import io.netty.handler.timeout.ReadTimeoutException
import no.novari.fint.model.resource.Link
import no.novari.fint.model.resource.arkiv.noark.JournalpostResource
import no.novari.flyt.archive.gateway.dispatch.file.FilesDispatchService
import no.novari.flyt.archive.gateway.dispatch.file.result.FilesDispatchResult
import no.novari.flyt.archive.gateway.dispatch.journalpost.result.RecordDispatchResult
import no.novari.flyt.archive.gateway.dispatch.mapping.JournalpostMappingService
import no.novari.flyt.archive.gateway.dispatch.model.instance.DokumentbeskrivelseDto
import no.novari.flyt.archive.gateway.dispatch.model.instance.DokumentobjektDto
import no.novari.flyt.archive.gateway.dispatch.model.instance.JournalpostDto
import no.novari.flyt.archive.gateway.dispatch.web.FintArchiveDispatchClient
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
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class RecordDispatchServiceTest {
    @Mock
    private lateinit var journalpostMappingService: JournalpostMappingService

    @Mock
    private lateinit var filesDispatchService: FilesDispatchService

    @Mock
    private lateinit var fintArchiveDispatchClient: FintArchiveDispatchClient

    @InjectMocks
    private lateinit var recordDispatchService: RecordDispatchService

    @Test
    fun givenAcceptedFilesAndAcceptedPostRecordShouldReturnAcceptedResult() {
        val fileId = UUID.randomUUID()
        val journalpostDto =
            JournalpostDto
                .builder()
                .dokumentbeskrivelse(
                    listOf(
                        DokumentbeskrivelseDto
                            .builder()
                            .dokumentobjekt(
                                listOf(DokumentobjektDto.builder().fileId(fileId).build()),
                            ).build(),
                    ),
                ).build()
        val journalpostResource: JournalpostResource = mock()
        val resultJournalpostResource: JournalpostResource = mock()
        whenever(resultJournalpostResource.journalPostnummer).thenReturn(1L)
        whenever(filesDispatchService.dispatch(journalpostDto.dokumentbeskrivelse!!.first().dokumentobjekt!!))
            .thenReturn(Mono.just(FilesDispatchResult.accepted(mapOf(fileId to Link.with("file")))))
        whenever(journalpostMappingService.toJournalpostResource(journalpostDto, mapOf(fileId to Link.with("file"))))
            .thenReturn(journalpostResource)
        whenever(
            fintArchiveDispatchClient.postRecord("caseId", journalpostResource),
        ).thenReturn(Mono.just(resultJournalpostResource))

        StepVerifier
            .create(recordDispatchService.dispatch("caseId", journalpostDto))
            .expectNext(RecordDispatchResult.accepted(1L))
            .verifyComplete()
    }

    @Test
    fun givenDeclinedFilesShouldReturnDeclinedResult() {
        val fileId = UUID.randomUUID()
        val journalpostDto =
            JournalpostDto
                .builder()
                .dokumentbeskrivelse(
                    listOf(
                        DokumentbeskrivelseDto
                            .builder()
                            .dokumentobjekt(
                                listOf(DokumentobjektDto.builder().fileId(fileId).build()),
                            ).build(),
                    ),
                ).build()
        whenever(filesDispatchService.dispatch(journalpostDto.dokumentbeskrivelse!!.first().dokumentobjekt!!))
            .thenReturn(Mono.just(FilesDispatchResult.declined("bad file")))

        StepVerifier
            .create(recordDispatchService.dispatch("caseId", journalpostDto))
            .expectNext(RecordDispatchResult.declined("Dokumentobjekt declined by destination with message='bad file'"))
            .verifyComplete()
    }

    @Test
    fun givenWebClientErrorShouldReturnDeclinedResult() {
        val journalpostDto = JournalpostDto.builder().build()
        val journalpostResource: JournalpostResource = mock()
        val error: WebClientResponseException = mock()
        whenever(error.responseBodyAsString).thenReturn("test response body")
        whenever(
            journalpostMappingService.toJournalpostResource(journalpostDto, emptyMap()),
        ).thenReturn(journalpostResource)
        whenever(fintArchiveDispatchClient.postRecord("caseId", journalpostResource)).thenReturn(Mono.error(error))

        StepVerifier
            .create(recordDispatchService.dispatch("caseId", journalpostDto))
            .expectNext(RecordDispatchResult.declined("test response body"))
            .verifyComplete()
    }

    @Test
    fun givenTimeoutShouldReturnTimedOutResult() {
        val journalpostDto = JournalpostDto.builder().build()
        val journalpostResource: JournalpostResource = mock()
        whenever(
            journalpostMappingService.toJournalpostResource(journalpostDto, emptyMap()),
        ).thenReturn(journalpostResource)
        whenever(
            fintArchiveDispatchClient.postRecord("caseId", journalpostResource),
        ).thenReturn(Mono.error(ReadTimeoutException()))

        StepVerifier
            .create(recordDispatchService.dispatch("caseId", journalpostDto))
            .expectNext(RecordDispatchResult.timedOut())
            .verifyComplete()
    }
}
