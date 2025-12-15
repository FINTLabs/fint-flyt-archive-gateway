package no.novari.flyt.archive.gateway.dispatch;

import no.novari.flyt.archive.gateway.dispatch.journalpost.RecordsDispatchService;
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
class RecordsProcessingServiceTest {

    @Mock
    private RecordsDispatchService recordsDispatchService;
    @Mock
    private DispatchMessageFormattingService dispatchMessageFormattingService;
    @InjectMocks
    private RecordsProcessingService recordsProcessingService;


    @Test
    public void givenAcceptedJournalpostDispatchShouldReturnAcceptedDispatchResultWithCaseIdAndJournalpostid() {
        JournalpostDto journalpostDto = mock(JournalpostDto.class);
        RecordsDispatchResult recordsDispatchResult = RecordsDispatchResult.accepted(List.of(1L));
        doReturn(Mono.just(recordsDispatchResult)).when(recordsDispatchService).dispatch(
                "testCaseId",
                List.of(journalpostDto)
        );

        doReturn("test format case id and journalpost ids").when(dispatchMessageFormattingService)
                .formatCaseIdAndJournalpostIds(
                        "testCaseId",
                        List.of(1L)
                );

        StepVerifier.create(
                        recordsProcessingService.processRecords(
                                "testCaseId",
                                true,
                                List.of(journalpostDto)
                        )
                )
                .expectNext(DispatchResult.accepted("test format case id and journalpost ids"))
                .verifyComplete();

        verify(recordsDispatchService, times(1)).dispatch(
                "testCaseId",
                List.of(journalpostDto)
        );
        verifyNoMoreInteractions(recordsDispatchService);
    }

    @Test
    public void givenNewCaseAndDeclinedJournalpostDispatchShouldReturnDeclinedDispatchResultWithErrorMessage() {
        JournalpostDto journalpostDto = mock(JournalpostDto.class);
        RecordsDispatchResult recordsDispatchResult = RecordsDispatchResult.declined(
                "test error message",
                "test warning message"
        );
        doReturn(Mono.just(recordsDispatchResult)).when(recordsDispatchService).dispatch(
                "testCaseId",
                List.of(journalpostDto)
        );

        doReturn(Optional.of("test combined functional warning message")).when(dispatchMessageFormattingService)
                .combineFunctionalWarningMessages(
                        "testCaseId",
                        true,
                        List.of("test warning message")
                );

        StepVerifier.create(
                        recordsProcessingService.processRecords(
                                "testCaseId",
                                true,
                                List.of(journalpostDto)
                        )
                )
                .expectNext(DispatchResult.declined(
                        "Journalpost was declined by the destination.  " +
                                "test combined functional warning message " +
                                "Error message: 'test error message'"
                ))
                .verifyComplete();
    }

    @Test
    public void givenNewCaseAndFailedJournalpostDispatchShouldReturnFailedDispatchResultWithErrorMessage() {
        JournalpostDto journalpostDto = mock(JournalpostDto.class);
        RecordsDispatchResult recordsDispatchResult = RecordsDispatchResult.failed(
                "test error message",
                "test warning message"
        );
        doReturn(Mono.just(recordsDispatchResult)).when(recordsDispatchService).dispatch(
                "testCaseId",
                List.of(journalpostDto)
        );

        doReturn(Optional.of("test combined functional warning message")).when(dispatchMessageFormattingService)
                .combineFunctionalWarningMessages(
                        "testCaseId",
                        true,
                        List.of("test warning message")
                );

        StepVerifier.create(
                        recordsProcessingService.processRecords(
                                "testCaseId",
                                true,
                                List.of(journalpostDto)
                        )
                )
                .expectNext(DispatchResult.failed(
                        "Journalpost dispatch failed. " +
                                "test combined functional warning message"
                ))
                .verifyComplete();
    }

}