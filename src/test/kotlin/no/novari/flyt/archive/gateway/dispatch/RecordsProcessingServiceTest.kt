package no.novari.flyt.archive.gateway.dispatch

import no.novari.flyt.archive.gateway.dispatch.journalpost.RecordsDispatchService
import no.novari.flyt.archive.gateway.dispatch.journalpost.result.RecordsDispatchResult
import no.novari.flyt.archive.gateway.dispatch.model.instance.JournalpostDto
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

@ExtendWith(MockitoExtension::class)
class RecordsProcessingServiceTest {
    @Mock
    private lateinit var recordsDispatchService: RecordsDispatchService

    @Mock
    private lateinit var dispatchMessageFormattingService: DispatchMessageFormattingService

    @InjectMocks
    private lateinit var recordsProcessingService: RecordsProcessingService

    @Test
    fun givenAcceptedJournalpostDispatchShouldReturnAcceptedDispatchResultWithCaseIdAndJournalpostId() {
        val journalpostDto = JournalpostDto.builder().build()
        whenever(recordsDispatchService.dispatch("testCaseId", listOf(journalpostDto)))
            .thenReturn(Mono.just(RecordsDispatchResult.accepted(listOf(1L))))
        whenever(dispatchMessageFormattingService.formatCaseIdAndJournalpostIds("testCaseId", listOf(1L)))
            .thenReturn("test format case id and journalpost ids")

        StepVerifier
            .create(recordsProcessingService.processRecords("testCaseId", true, listOf(journalpostDto)))
            .expectNext(DispatchResult.accepted("test format case id and journalpost ids"))
            .verifyComplete()
    }

    @Test
    fun givenNewCaseAndDeclinedJournalpostDispatchShouldReturnDeclinedDispatchResultWithErrorMessage() {
        val journalpostDto = JournalpostDto.builder().build()
        whenever(recordsDispatchService.dispatch("testCaseId", listOf(journalpostDto)))
            .thenReturn(Mono.just(RecordsDispatchResult.declined("test error message", "test warning message")))
        whenever(
            dispatchMessageFormattingService.combineFunctionalWarningMessagesOrNull(
                "testCaseId",
                true,
                listOf("test warning message"),
            ),
        ).thenReturn("test combined functional warning message")

        StepVerifier
            .create(recordsProcessingService.processRecords("testCaseId", true, listOf(journalpostDto)))
            .expectNext(
                DispatchResult.declined(
                    "Journalpost was declined by the destination.  test combined functional warning message Error message: 'test error message'",
                ),
            ).verifyComplete()
    }

    @Test
    fun givenNewCaseAndFailedJournalpostDispatchShouldReturnFailedDispatchResult() {
        val journalpostDto = JournalpostDto.builder().build()
        whenever(recordsDispatchService.dispatch("testCaseId", listOf(journalpostDto)))
            .thenReturn(Mono.just(RecordsDispatchResult.failed("test error message", "test warning message")))
        whenever(
            dispatchMessageFormattingService.combineFunctionalWarningMessagesOrNull(
                "testCaseId",
                true,
                listOf("test warning message"),
            ),
        ).thenReturn("test combined functional warning message")

        StepVerifier
            .create(recordsProcessingService.processRecords("testCaseId", true, listOf(journalpostDto)))
            .expectNext(DispatchResult.failed("Journalpost dispatch failed. test combined functional warning message"))
            .verifyComplete()
    }
}
