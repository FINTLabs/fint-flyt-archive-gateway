package no.novari.flyt.archive.gateway.dispatch.journalpost

import no.novari.flyt.archive.gateway.dispatch.DispatchMessageFormattingService
import no.novari.flyt.archive.gateway.dispatch.journalpost.result.RecordDispatchResult
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
class RecordsDispatchServiceTest {
    @Mock
    private lateinit var recordDispatchService: RecordDispatchService

    @Mock
    private lateinit var dispatchMessageFormattingService: DispatchMessageFormattingService

    @InjectMocks
    private lateinit var recordsDispatchService: RecordsDispatchService

    @Test
    fun givenNoJournalpostsShouldReturnAcceptedResult() {
        StepVerifier
            .create(recordsDispatchService.dispatch("caseId", emptyList()))
            .expectNext(RecordsDispatchResult.accepted(emptyList()))
            .verifyComplete()
    }

    @Test
    fun givenAcceptedJournalpostsShouldReturnAcceptedResult() {
        val journalpostDto1 = JournalpostDto.builder().tittel("jp1").build()
        val journalpostDto2 = JournalpostDto.builder().tittel("jp2").build()
        whenever(
            recordDispatchService.dispatch("caseId", journalpostDto1),
        ).thenReturn(Mono.just(RecordDispatchResult.accepted(1L)))
        whenever(
            recordDispatchService.dispatch("caseId", journalpostDto2),
        ).thenReturn(Mono.just(RecordDispatchResult.accepted(2L)))

        StepVerifier
            .create(recordsDispatchService.dispatch("caseId", listOf(journalpostDto1, journalpostDto2)))
            .expectNext(RecordsDispatchResult.accepted(listOf(1L, 2L)))
            .verifyComplete()
    }

    @Test
    fun givenDeclinedLastRecordShouldReturnDeclinedResultWithWarningMessage() {
        val journalpostDto1 = JournalpostDto.builder().tittel("jp1").build()
        val journalpostDto2 = JournalpostDto.builder().tittel("jp2").build()
        whenever(
            recordDispatchService.dispatch("caseId", journalpostDto1),
        ).thenReturn(Mono.just(RecordDispatchResult.accepted(1L)))
        whenever(
            recordDispatchService.dispatch("caseId", journalpostDto2),
        ).thenReturn(Mono.just(RecordDispatchResult.declined("test error message")))
        whenever(
            dispatchMessageFormattingService.createFunctionalWarningMessageOrNull("journalpost", "id", listOf("1")),
        ).thenReturn("test warning message")

        StepVerifier
            .create(recordsDispatchService.dispatch("caseId", listOf(journalpostDto1, journalpostDto2)))
            .expectNext(RecordsDispatchResult.declined("test error message", "test warning message"))
            .verifyComplete()
    }
}
