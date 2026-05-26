package no.novari.flyt.archive.gateway.dispatch.journalpost

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
import org.assertj.core.api.Assertions.assertThat
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
    fun `given accepted files and an accepted postRecord, returns an accepted result`() {
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
            .thenReturn(FilesDispatchResult.accepted(mapOf(fileId to Link.with("file"))))
        whenever(journalpostMappingService.toJournalpostResource(journalpostDto, mapOf(fileId to Link.with("file"))))
            .thenReturn(journalpostResource)
        whenever(fintArchiveDispatchClient.postRecord("caseId", journalpostResource))
            .thenReturn(resultJournalpostResource)

        val result = recordDispatchService.dispatch("caseId", journalpostDto)

        assertThat(result).isEqualTo(RecordDispatchResult.accepted(1L))
    }

    @Test
    fun `given declined files, returns a declined result`() {
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
            .thenReturn(FilesDispatchResult.declined("bad file"))

        val result = recordDispatchService.dispatch("caseId", journalpostDto)

        assertThat(result)
            .isEqualTo(RecordDispatchResult.declined("Dokumentobjekt declined by destination with message='bad file'"))
    }

    @Test
    fun `given a RestClientResponseException, returns a declined result`() {
        val journalpostDto = JournalpostDto.builder().build()
        val journalpostResource: JournalpostResource = mock()
        val error: HttpClientErrorException = mock()
        whenever(error.responseBodyAsString).thenReturn("test response body")
        whenever(
            journalpostMappingService.toJournalpostResource(journalpostDto, emptyMap()),
        ).thenReturn(journalpostResource)
        whenever(fintArchiveDispatchClient.postRecord("caseId", journalpostResource)).thenThrow(error)

        val result = recordDispatchService.dispatch("caseId", journalpostDto)

        assertThat(result).isEqualTo(RecordDispatchResult.declined("test response body"))
    }

    @Test
    fun `given a read timeout, returns a timed-out result`() {
        val journalpostDto = JournalpostDto.builder().build()
        val journalpostResource: JournalpostResource = mock()
        whenever(
            journalpostMappingService.toJournalpostResource(journalpostDto, emptyMap()),
        ).thenReturn(journalpostResource)
        whenever(fintArchiveDispatchClient.postRecord("caseId", journalpostResource))
            .thenThrow(ResourceAccessException("read timeout", HttpTimeoutException("read timeout")))

        val result = recordDispatchService.dispatch("caseId", journalpostDto)

        assertThat(result).isEqualTo(RecordDispatchResult.timedOut())
    }
}
