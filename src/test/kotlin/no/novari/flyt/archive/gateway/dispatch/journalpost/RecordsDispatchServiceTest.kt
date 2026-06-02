package no.novari.flyt.archive.gateway.dispatch.journalpost

import no.novari.flyt.archive.gateway.dispatch.DispatchMessageFormattingService
import no.novari.flyt.archive.gateway.dispatch.journalpost.result.RecordDispatchResult
import no.novari.flyt.archive.gateway.dispatch.journalpost.result.RecordsDispatchResult
import no.novari.flyt.archive.gateway.dispatch.model.instance.JournalpostDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class RecordsDispatchServiceTest {
    @Mock
    private lateinit var recordDispatchService: RecordDispatchService

    @Mock
    private lateinit var dispatchMessageFormattingService: DispatchMessageFormattingService

    @InjectMocks
    private lateinit var recordsDispatchService: RecordsDispatchService

    @Test
    fun `given no journalposts, returns an accepted result`() {
        val result = recordsDispatchService.dispatch("caseId", emptyList())

        assertThat(result).isEqualTo(RecordsDispatchResult.accepted(emptyList()))
    }

    @Test
    fun `given accepted journalposts, returns an accepted result`() {
        val journalpostDto1 = JournalpostDto.builder().tittel("jp1").build()
        val journalpostDto2 = JournalpostDto.builder().tittel("jp2").build()
        whenever(recordDispatchService.dispatch("caseId", journalpostDto1))
            .thenReturn(RecordDispatchResult.accepted(1L))
        whenever(recordDispatchService.dispatch("caseId", journalpostDto2))
            .thenReturn(RecordDispatchResult.accepted(2L))

        val result = recordsDispatchService.dispatch("caseId", listOf(journalpostDto1, journalpostDto2))

        assertThat(result).isEqualTo(RecordsDispatchResult.accepted(listOf(1L, 2L)))
    }

    @Test
    fun `given a declined last record, returns a declined result with warning message`() {
        val journalpostDto1 = JournalpostDto.builder().tittel("jp1").build()
        val journalpostDto2 = JournalpostDto.builder().tittel("jp2").build()
        whenever(recordDispatchService.dispatch("caseId", journalpostDto1))
            .thenReturn(RecordDispatchResult.accepted(1L))
        whenever(recordDispatchService.dispatch("caseId", journalpostDto2))
            .thenReturn(RecordDispatchResult.declined("test error message"))
        whenever(
            dispatchMessageFormattingService.createFunctionalWarningMessageOrNull("journalpost", "id", listOf("1")),
        ).thenReturn("test warning message")

        val result = recordsDispatchService.dispatch("caseId", listOf(journalpostDto1, journalpostDto2))

        assertThat(result).isEqualTo(RecordsDispatchResult.declined("test error message", "test warning message"))
    }
}
