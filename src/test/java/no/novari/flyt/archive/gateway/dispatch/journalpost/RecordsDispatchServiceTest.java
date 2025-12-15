package no.novari.flyt.archive.gateway.dispatch.journalpost;

import no.novari.flyt.archive.gateway.dispatch.DispatchMessageFormattingService;
import no.novari.flyt.archive.gateway.dispatch.journalpost.result.RecordDispatchResult;
import no.novari.flyt.archive.gateway.dispatch.journalpost.result.RecordsDispatchResult;
import no.novari.flyt.archive.gateway.dispatch.model.instance.JournalpostDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class RecordsDispatchServiceTest {

    @Mock
    private RecordDispatchService recordDispatchService;
    @Mock
    private DispatchMessageFormattingService dispatchMessageFormattingService;
    @InjectMocks
    private RecordsDispatchService recordsDispatchService;

    @Test
    public void givenNoJournalpostDtosShouldReturnAcceptedResultWithNoJournalpostIds() {
        StepVerifier.create(
                        recordsDispatchService.dispatch("testCaseId", List.of())
                )
                .expectNext(RecordsDispatchResult.accepted(List.of()))
                .verifyComplete();
    }

    @Test
    public void givenAcceptedJournalpostDtosShouldReturnAcceptedResultWithAllSuccessfulJournalpostIds() {
        final String caseId = "testCaseId";
        JournalpostDto journalpostDto1 = mock(JournalpostDto.class);
        JournalpostDto journalpostDto2 = mock(JournalpostDto.class);

        doReturn(Mono.just(RecordDispatchResult.accepted(1L))).when(recordDispatchService).dispatch(caseId, journalpostDto1);
        doReturn(Mono.just(RecordDispatchResult.accepted(2L))).when(recordDispatchService).dispatch(caseId, journalpostDto2);

        StepVerifier.create(
                        recordsDispatchService.dispatch(caseId, List.of(
                                journalpostDto1,
                                journalpostDto2
                        ))
                )
                .expectNext(RecordsDispatchResult.accepted(List.of(1L, 2L)))
                .verifyComplete();

        verify(recordDispatchService, times(1)).dispatch(caseId, journalpostDto1);
        verify(recordDispatchService, times(1)).dispatch(caseId, journalpostDto2);
        verifyNoMoreInteractions(recordDispatchService);
    }

    @Test
    public void givenAcceptedJournalpostsAndLastOneDeclinedShouldReturnDeclinedResultWithErrorMessageAndWarningMessage() {
        final String caseId = "testCaseId";
        JournalpostDto journalpostDto1 = mock(JournalpostDto.class);
        JournalpostDto journalpostDto2 = mock(JournalpostDto.class);
        JournalpostDto journalpostDto3 = mock(JournalpostDto.class);

        doReturn(Optional.of("test warning message")).when(dispatchMessageFormattingService).createFunctionalWarningMessage("journalpost", "id", List.of("1", "2"));

        doReturn(Mono.just(RecordDispatchResult.accepted(1L))).when(recordDispatchService).dispatch(caseId, journalpostDto1);
        doReturn(Mono.just(RecordDispatchResult.accepted(2L))).when(recordDispatchService).dispatch(caseId, journalpostDto2);
        doReturn(Mono.just(RecordDispatchResult.declined("test error message"))).when(recordDispatchService).dispatch(caseId, journalpostDto3);

        StepVerifier.create(
                        recordsDispatchService.dispatch(caseId, List.of(
                                journalpostDto1,
                                journalpostDto2,
                                journalpostDto3
                        ))
                )
                .expectNext(RecordsDispatchResult.declined(
                        "test error message",
                        "test warning message"

                ))
                .verifyComplete();

        verify(dispatchMessageFormattingService, times(1)).createFunctionalWarningMessage("journalpost", "id", List.of("1", "2"));
        verifyNoMoreInteractions(dispatchMessageFormattingService);

        verify(recordDispatchService, times(1)).dispatch(caseId, journalpostDto1);
        verify(recordDispatchService, times(1)).dispatch(caseId, journalpostDto2);
        verify(recordDispatchService, times(1)).dispatch(caseId, journalpostDto3);
        verifyNoMoreInteractions(recordDispatchService);
    }

    @Test
    public void givenAcceptedJournalpostsAndLastOneFailedWithoutWarningShouldReturnFailedResultWithErrorMessageAndWarningMessage() {
        final String caseId = "testCaseId";
        JournalpostDto journalpostDto1 = mock(JournalpostDto.class);
        JournalpostDto journalpostDto2 = mock(JournalpostDto.class);
        JournalpostDto journalpostDto3 = mock(JournalpostDto.class);

        doReturn(Optional.of("test warning message")).when(dispatchMessageFormattingService).createFunctionalWarningMessage("journalpost", "id", List.of("1", "2"));

        doReturn(Mono.just(RecordDispatchResult.accepted(1L))).when(recordDispatchService).dispatch(caseId, journalpostDto1);
        doReturn(Mono.just(RecordDispatchResult.accepted(2L))).when(recordDispatchService).dispatch(caseId, journalpostDto2);
        doReturn(Mono.just(RecordDispatchResult.failed("test error message"))).when(recordDispatchService).dispatch(caseId, journalpostDto3);

        StepVerifier.create(
                        recordsDispatchService.dispatch(caseId, List.of(
                                journalpostDto1,
                                journalpostDto2,
                                journalpostDto3
                        ))
                )
                .expectNext(RecordsDispatchResult.failed(
                        "test error message",
                        "test warning message"

                ))
                .verifyComplete();

        verify(dispatchMessageFormattingService, times(1)).createFunctionalWarningMessage("journalpost", "id", List.of("1", "2"));
        verifyNoMoreInteractions(dispatchMessageFormattingService);

        verify(recordDispatchService, times(1)).dispatch(caseId, journalpostDto1);
        verify(recordDispatchService, times(1)).dispatch(caseId, journalpostDto2);
        verify(recordDispatchService, times(1)).dispatch(caseId, journalpostDto3);
        verifyNoMoreInteractions(recordDispatchService);
    }

    @Test
    public void givenMonoErrorFromRecordDispatchShouldReturnFailedResult() {
        final String caseId = "testCaseId";
        JournalpostDto journalpostDto = mock(JournalpostDto.class);

        doReturn(Mono.error(new RuntimeException())).when(recordDispatchService).dispatch(caseId, journalpostDto);

        StepVerifier.create(
                        recordsDispatchService.dispatch(caseId, List.of(
                                journalpostDto
                        ))
                )
                .expectNext(RecordsDispatchResult.failed("Journalposts dispatch failed", "possible journalposts with unknown ids"))
                .verifyComplete();

        verify(recordDispatchService, times(1)).dispatch(caseId, journalpostDto);
        verifyNoMoreInteractions(recordDispatchService);
    }

}