package no.novari.flyt.archive.gateway.dispatch

import no.novari.flyt.archive.gateway.dispatch.journalpost.RecordsDispatchService
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
class RecordsProcessingServiceTest {
    @Mock
    private lateinit var recordsDispatchService: RecordsDispatchService

    @Mock
    private lateinit var dispatchMessageFormattingService: DispatchMessageFormattingService

    @InjectMocks
    private lateinit var recordsProcessingService: RecordsProcessingService

    @Test
    fun `given an accepted journalpost dispatch, returns an accepted result with case id and journalpost id`() {
        val journalpostDto = JournalpostDto.builder().build()
        whenever(recordsDispatchService.dispatch("testCaseId", listOf(journalpostDto)))
            .thenReturn(RecordsDispatchResult.accepted(listOf(1L)))
        whenever(dispatchMessageFormattingService.formatCaseIdAndJournalpostIds("testCaseId", listOf(1L)))
            .thenReturn("test format case id and journalpost ids")

        val result = recordsProcessingService.processRecords("testCaseId", true, listOf(journalpostDto))

        assertThat(result).isEqualTo(DispatchResult.accepted("test format case id and journalpost ids"))
    }

    @Test
    fun `given a new case and a declined journalpost dispatch, returns a declined result with error message`() {
        val journalpostDto = JournalpostDto.builder().build()
        whenever(recordsDispatchService.dispatch("testCaseId", listOf(journalpostDto)))
            .thenReturn(RecordsDispatchResult.declined("test error message", "test warning message"))
        whenever(
            dispatchMessageFormattingService.combineFunctionalWarningMessagesOrNull(
                "testCaseId",
                true,
                listOf("test warning message"),
            ),
        ).thenReturn("test combined functional warning message")

        val result = recordsProcessingService.processRecords("testCaseId", true, listOf(journalpostDto))

        assertThat(result).isEqualTo(
            DispatchResult.declined(
                "Journalpost was declined by the destination.  test combined functional warning message " +
                    "Error message: 'test error message'",
            ),
        )
    }

    @Test
    fun `given a new case and a failed journalpost dispatch, returns a failed result`() {
        val journalpostDto = JournalpostDto.builder().build()
        whenever(recordsDispatchService.dispatch("testCaseId", listOf(journalpostDto)))
            .thenReturn(RecordsDispatchResult.failed("test error message", "test warning message"))
        whenever(
            dispatchMessageFormattingService.combineFunctionalWarningMessagesOrNull(
                "testCaseId",
                true,
                listOf("test warning message"),
            ),
        ).thenReturn("test combined functional warning message")

        val result = recordsProcessingService.processRecords("testCaseId", true, listOf(journalpostDto))

        assertThat(result).isEqualTo(
            DispatchResult.failed("Journalpost dispatch failed. test combined functional warning message"),
        )
    }
}
