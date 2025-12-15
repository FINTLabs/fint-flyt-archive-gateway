package no.novari.flyt.archive.gateway.dispatch;

import no.novari.flyt.archive.gateway.dispatch.model.CaseDispatchType;
import no.novari.flyt.archive.gateway.dispatch.model.instance.ArchiveInstance;
import no.novari.flyt.archive.gateway.dispatch.model.instance.JournalpostDto;
import no.novari.flyt.archive.gateway.dispatch.model.instance.SakDto;
import no.novari.flyt.archive.gateway.dispatch.sak.CaseDispatchService;
import no.novari.flyt.archive.gateway.dispatch.sak.result.CaseDispatchResult;
import no.novari.flyt.archive.gateway.dispatch.sak.result.CaseSearchResult;
import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeaders;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Optional;

import static no.novari.flyt.archive.gateway.dispatch.DispatchStatus.ACCEPTED;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class DispatchServiceTest {

    @Mock
    private CaseDispatchService caseDispatchService;
    @Mock
    private InstanceFlowHeaders instanceFlowHeaders;
    @Mock
    private ArchiveInstance archiveInstance;
    @Mock
    private RecordsProcessingService recordsProcessingService;
    @InjectMocks
    private DispatchService dispatchService;

    @Test
    public void givenCaseTypeNewAndAcceptedCaseDispatchWithNoJournalpostShouldReturnAcceptedResultWithCaseId() {
        doReturn(CaseDispatchType.NEW).when(archiveInstance).getType();

        SakDto sakDto = mock(SakDto.class);
        doReturn(sakDto).when(archiveInstance).getNewCase();
        doReturn(Mono.just(CaseDispatchResult.accepted("testCaseId")))
                .when(caseDispatchService).dispatch(sakDto);

        StepVerifier.create(
                        dispatchService.process(instanceFlowHeaders, archiveInstance)
                )
                .expectNext(DispatchResult.accepted("testCaseId"))
                .verifyComplete();

        verify(archiveInstance, times(1)).getType();
        verify(archiveInstance, times(2)).getNewCase();
        verifyNoMoreInteractions(archiveInstance);

        verify(caseDispatchService, times(1)).dispatch(sakDto);
        verifyNoMoreInteractions(caseDispatchService);
    }

    @Test
    public void givenCaseTypeNewAndAcceptedCaseDispatchShouldCallRecordsProcessingServiceAndReturnResult() {
        doReturn(CaseDispatchType.NEW).when(archiveInstance).getType();

        SakDto sakDto = mock(SakDto.class);
        doReturn(sakDto).when(archiveInstance).getNewCase();
        doReturn(Mono.just(CaseDispatchResult.accepted("testCaseId")))
                .when(caseDispatchService).dispatch(sakDto);

        JournalpostDto journalpostDto = mock(JournalpostDto.class);
        doReturn(Optional.of(List.of(journalpostDto))).when(sakDto).getJournalpost();

        doReturn(Mono.just(DispatchResult.accepted("testCaseId")))
                .when(recordsProcessingService)
                .processRecords("testCaseId", true, List.of(journalpostDto));

        StepVerifier.create(
                        dispatchService.process(instanceFlowHeaders, archiveInstance)
                )
                .expectNext(DispatchResult.accepted("testCaseId"))
                .verifyComplete();

        verify(archiveInstance, times(1)).getType();
        verify(archiveInstance, times(2)).getNewCase();
        verifyNoMoreInteractions(archiveInstance);

        verify(caseDispatchService, times(1)).dispatch(sakDto);
        verifyNoMoreInteractions(caseDispatchService);

        verify(recordsProcessingService, times(1)).
                processRecords("testCaseId", true, List.of(journalpostDto));
        verifyNoMoreInteractions(recordsProcessingService);
    }

    @Test
    public void givenCaseTypeNewAndDeclinedCaseDispatchShouldReturnDeclinedResultWithErrorMessage() {
        doReturn(CaseDispatchType.NEW).when(archiveInstance).getType();

        SakDto sakDto = mock(SakDto.class);
        doReturn(sakDto).when(archiveInstance).getNewCase();

        doReturn(Mono.just(CaseDispatchResult.declined("test error message")))
                .when(caseDispatchService)
                .dispatch(sakDto);

        StepVerifier.create(
                        dispatchService.process(instanceFlowHeaders, archiveInstance)
                )
                .expectNext(DispatchResult
                        .declined("Sak was declined by the destination with message='test error message'")
                )
                .verifyComplete();

        verify(archiveInstance, times(1)).getType();
        verify(archiveInstance, times(1)).getNewCase();
        verifyNoMoreInteractions(archiveInstance);

        verify(caseDispatchService, times(1)).dispatch(sakDto);
        verifyNoMoreInteractions(caseDispatchService);
    }

    @Test
    public void givenCaseTypeNewAndFailedCaseDispatchShouldReturnFailedResultWithErrorMessage() {
        doReturn(CaseDispatchType.NEW).when(archiveInstance).getType();

        SakDto sakDto = mock(SakDto.class);
        doReturn(sakDto).when(archiveInstance).getNewCase();

        doReturn(Mono.just(CaseDispatchResult.failed())).when(caseDispatchService).dispatch(sakDto);

        StepVerifier.create(
                        dispatchService.process(instanceFlowHeaders, archiveInstance)
                )
                .expectNext(DispatchResult.failed("Sak dispatch failed"))
                .verifyComplete();

        verify(archiveInstance, times(1)).getType();
        verify(archiveInstance, times(1)).getNewCase();
        verifyNoMoreInteractions(archiveInstance);

        verify(caseDispatchService, times(1)).dispatch(sakDto);
        verifyNoMoreInteractions(caseDispatchService);
    }

    @Test
    public void givenCaseTypeByIdAndAcceptedJournalpostDispatchShouldCallRecordsProcessingServiceWithNewCaseFalseAndReturnResult() {
        doReturn(CaseDispatchType.BY_ID).when(archiveInstance).getType();
        doReturn("testCaseId").when(archiveInstance).getCaseId();

        JournalpostDto journalpostDto = mock(JournalpostDto.class);
        doReturn(List.of(journalpostDto)).when(archiveInstance).getJournalpost();

        doReturn(Mono.just(DispatchResult.accepted("testCaseId")))
                .when(recordsProcessingService)
                .processRecords("testCaseId", false, List.of(journalpostDto));

        StepVerifier.create(
                        dispatchService.process(instanceFlowHeaders, archiveInstance)
                )
                .expectNext(DispatchResult.accepted("testCaseId"))
                .verifyComplete();

        verify(archiveInstance, times(1)).getType();
        verify(archiveInstance, times(1)).getCaseId();
        verifyNoMoreInteractions(archiveInstance);

        verify(recordsProcessingService, times(1))
                .processRecords("testCaseId", false, List.of(journalpostDto));
        verifyNoMoreInteractions(recordsProcessingService);
    }

    @Test
    public void givenCaseTypeByIdAndDeclinedJournalpostDispatchShouldCallRecordsProcessingServiceWithNewCaseFalseAndReturnResult() {
        doReturn(CaseDispatchType.BY_ID).when(archiveInstance).getType();
        doReturn("testCaseId").when(archiveInstance).getCaseId();

        JournalpostDto journalpostDto = mock(JournalpostDto.class);
        doReturn(List.of(journalpostDto)).when(archiveInstance).getJournalpost();

        doReturn(Mono.just(DispatchResult.declined("test error message")))
                .when(recordsProcessingService)
                .processRecords("testCaseId", false, List.of(journalpostDto));

        StepVerifier.create(
                        dispatchService.process(instanceFlowHeaders, archiveInstance)
                )
                .expectNext(DispatchResult.declined(
                        "test error message"
                ))
                .verifyComplete();

        verify(archiveInstance, times(1)).getType();
        verify(archiveInstance, times(1)).getCaseId();
        verifyNoMoreInteractions(archiveInstance);

        verify(recordsProcessingService, times(1))
                .processRecords("testCaseId", false, List.of(journalpostDto));
        verifyNoMoreInteractions(recordsProcessingService);
    }

    @Test
    public void givenCaseByIdAndFailedJournalpostDispatchShouldCallRecordsProcessingServiceWithNewCaseFalseAndReturnResult() {
        doReturn(CaseDispatchType.BY_ID).when(archiveInstance).getType();
        doReturn("testCaseId").when(archiveInstance).getCaseId();

        JournalpostDto journalpostDto = mock(JournalpostDto.class);
        doReturn(List.of(journalpostDto)).when(archiveInstance).getJournalpost();

        doReturn(Mono.just(DispatchResult.failed("test error message")))
                .when(recordsProcessingService)
                .processRecords("testCaseId", false, List.of(journalpostDto));

        StepVerifier.create(
                        dispatchService.process(instanceFlowHeaders, archiveInstance)
                )
                .expectNext(DispatchResult.failed("test error message"
                ))
                .verifyComplete();

        verify(archiveInstance, times(1)).getType();
        verify(archiveInstance, times(1)).getCaseId();
        verifyNoMoreInteractions(archiveInstance);

        verify(recordsProcessingService, times(1))
                .processRecords("testCaseId", false, List.of(journalpostDto));
        verifyNoMoreInteractions(recordsProcessingService);
    }

    @Test
    public void givenCaseTypeBySearchOrNewWithJournalPostShouldReturnDeclinedResultWhenMultipleCasesAreFound() {
        doReturn(CaseDispatchType.BY_SEARCH_OR_NEW).when(archiveInstance).getType();

        SakDto sakDto = mock(SakDto.class);
        doReturn(sakDto).when(archiveInstance).getNewCase();

        JournalpostDto journalpostDto = mock(JournalpostDto.class);
        doReturn(Optional.of(List.of(journalpostDto))).when(sakDto).getJournalpost();

        CaseSearchResult caseSearchResult = mock(CaseSearchResult.class);
        doReturn(List.of("caseId1", "caseId2")).when(caseSearchResult).getArchiveCaseIds();
        doReturn(ACCEPTED).when(caseSearchResult).getStatus();

        doReturn(Mono.just(caseSearchResult)).when(caseDispatchService).findCasesBySearch(archiveInstance);

        StepVerifier.create(
                        dispatchService.process(instanceFlowHeaders, archiveInstance)
                )
                .expectNext(DispatchResult.declined("Found multiple cases: caseId1, caseId2"))
                .verifyComplete();

        verify(archiveInstance, times(1)).getType();
        verify(archiveInstance, times(1)).getNewCase();
        verifyNoMoreInteractions(archiveInstance);

        verify(caseDispatchService, times(1)).findCasesBySearch(archiveInstance);
        verifyNoMoreInteractions(caseDispatchService);
    }

    @Test
    public void givenCaseTypeBySearchOrNewWithJournalPostAndOneCaseIsFoundAndAcceptedJournalpostDispatchShouldCallRecordsProcessingServiceWithNewCaseFalseAndReturnResult() {
        doReturn(CaseDispatchType.BY_SEARCH_OR_NEW).when(archiveInstance).getType();

        SakDto sakDto = mock(SakDto.class);
        doReturn(sakDto).when(archiveInstance).getNewCase();

        JournalpostDto journalpostDto = mock(JournalpostDto.class);
        doReturn(Optional.of(List.of(journalpostDto))).when(sakDto).getJournalpost();

        CaseSearchResult caseSearchResult = mock(CaseSearchResult.class);
        doReturn(List.of("testCaseId")).when(caseSearchResult).getArchiveCaseIds();
        doReturn(ACCEPTED).when(caseSearchResult).getStatus();

        doReturn(Mono.just(caseSearchResult)).when(caseDispatchService).findCasesBySearch(archiveInstance);

        StepVerifier.create(
                        dispatchService.process(instanceFlowHeaders, archiveInstance)
                )
                .expectNext(DispatchResult.accepted("testCaseId"))
                .verifyComplete();

        verify(archiveInstance, times(1)).getType();
        verify(archiveInstance, times(1)).getNewCase();
        verifyNoMoreInteractions(archiveInstance);

        verify(caseDispatchService, times(1)).findCasesBySearch(archiveInstance);
        verifyNoMoreInteractions(caseDispatchService);

        verify(recordsProcessingService, times(1))
                .processRecords("testCaseId", false, List.of(journalpostDto));
        verifyNoMoreInteractions(recordsProcessingService);
    }

    @Test
    public void givenCaseTypeBySearchOrNewWithJournalPostAndNoCaseFoundShouldCallRecordsProcessingServiceWithNewCaseTrueAndReturnResult() {
        doReturn(CaseDispatchType.BY_SEARCH_OR_NEW).when(archiveInstance).getType();

        SakDto sakDto = mock(SakDto.class);
        doReturn(sakDto).when(archiveInstance).getNewCase();

        JournalpostDto journalpostDto = mock(JournalpostDto.class);
        doReturn(Optional.of(List.of(journalpostDto))).when(sakDto).getJournalpost();

        doReturn(Mono.just(CaseDispatchResult.accepted("testCaseId"))).when(caseDispatchService)
                .dispatch(sakDto);

        CaseSearchResult caseSearchResult = mock(CaseSearchResult.class);
        doReturn(List.of()).when(caseSearchResult).getArchiveCaseIds();
        doReturn(ACCEPTED).when(caseSearchResult).getStatus();

        doReturn(Mono.just(caseSearchResult)).when(caseDispatchService).findCasesBySearch(archiveInstance);

        doReturn(Mono.just(DispatchResult.accepted("testCaseId")))
                .when(recordsProcessingService)
                .processRecords("testCaseId", true, List.of(journalpostDto));

        StepVerifier.create(
                        dispatchService.process(instanceFlowHeaders, archiveInstance)
                )
                .expectNext(DispatchResult.accepted("testCaseId"))
                .verifyComplete();

        verify(archiveInstance, times(1)).getType();
        verify(archiveInstance, times(3)).getNewCase();
        verifyNoMoreInteractions(archiveInstance);

        verify(caseDispatchService, times(1)).findCasesBySearch(archiveInstance);
        verifyNoMoreInteractions(caseDispatchService);

        verify(recordsProcessingService, times(1))
                .processRecords("testCaseId", true, List.of(journalpostDto));
        verifyNoMoreInteractions(recordsProcessingService);
    }

    @Test
    public void givenCaseTypeBySearchOrNewAndDeclinedCaseSearchShouldReturnDeclinedResult() {
        doReturn(CaseDispatchType.BY_SEARCH_OR_NEW).when(archiveInstance).getType();

        SakDto sakDto = mock(SakDto.class);
        doReturn(sakDto).when(archiveInstance).getNewCase();
        doReturn(Optional.of(List.of())).when(sakDto).getJournalpost();

        CaseSearchResult caseSearchResult = mock(CaseSearchResult.class);
        doReturn(DispatchStatus.DECLINED).when(caseSearchResult).getStatus();
        doReturn("test errormessage").when(caseSearchResult).getErrorMessage();

        doReturn(Mono.just(caseSearchResult)).when(caseDispatchService).findCasesBySearch(archiveInstance);

        StepVerifier.create(
                        dispatchService.process(instanceFlowHeaders, archiveInstance)
                )
                .expectNext(DispatchResult.declined("test errormessage"))
                .verifyComplete();

        verify(archiveInstance, times(1)).getType();
        verify(archiveInstance, times(1)).getNewCase();
        verifyNoMoreInteractions(archiveInstance);

        verify(caseDispatchService, times(1)).findCasesBySearch(archiveInstance);
        verifyNoMoreInteractions(caseDispatchService);
    }

    @Test
    public void givenCaseTypeBySearchOrNewAndFailedCaseSearchShouldReturnFailedResult() {
        doReturn(CaseDispatchType.BY_SEARCH_OR_NEW).when(archiveInstance).getType();

        SakDto sakDto = mock(SakDto.class);
        doReturn(sakDto).when(archiveInstance).getNewCase();
        doReturn(Optional.of(List.of())).when(sakDto).getJournalpost();

        CaseSearchResult caseSearchResult = mock(CaseSearchResult.class);
        doReturn(DispatchStatus.FAILED).when(caseSearchResult).getStatus();

        doReturn(Mono.just(caseSearchResult)).when(caseDispatchService).findCasesBySearch(archiveInstance);

        StepVerifier.create(
                        dispatchService.process(instanceFlowHeaders, archiveInstance)
                )
                .expectNext(DispatchResult.failed())
                .verifyComplete();

        verify(archiveInstance, times(1)).getType();
        verify(archiveInstance, times(1)).getNewCase();
        verifyNoMoreInteractions(archiveInstance);

        verify(caseDispatchService, times(1)).findCasesBySearch(archiveInstance);
        verifyNoMoreInteractions(caseDispatchService);
    }

    @Test
    public void givenCaseTypeBySearchOrNewAndNoCaseFoundAndNewCaseDeclinedShouldReturnDeclinedResult() {
        doReturn(CaseDispatchType.BY_SEARCH_OR_NEW).when(archiveInstance).getType();

        SakDto sakDto = mock(SakDto.class);
        doReturn(sakDto).when(archiveInstance).getNewCase();
        doReturn(Optional.of(List.of())).when(sakDto).getJournalpost();

        CaseSearchResult caseSearchResult = mock(CaseSearchResult.class);
        doReturn(ACCEPTED).when(caseSearchResult).getStatus();
        doReturn(List.of()).when(caseSearchResult).getArchiveCaseIds();
        doReturn(Mono.just(caseSearchResult)).when(caseDispatchService).findCasesBySearch(archiveInstance);

        CaseDispatchResult caseDispatchResult = mock(CaseDispatchResult.class);
        doReturn(DispatchStatus.DECLINED).when(caseDispatchResult).getStatus();
        doReturn("testErrorMessage").when(caseDispatchResult).getErrorMessage();
        doReturn(Mono.just(caseDispatchResult)).when(caseDispatchService).dispatch(sakDto);

        StepVerifier.create(
                        dispatchService.process(instanceFlowHeaders, archiveInstance)
                )
                .expectNext(DispatchResult.declined("Sak was declined by the destination with message='testErrorMessage'"))
                .verifyComplete();

        verify(archiveInstance, times(1)).getType();
        verify(archiveInstance, times(2)).getNewCase();
        verifyNoMoreInteractions(archiveInstance);

        verify(caseDispatchService, times(1)).findCasesBySearch(archiveInstance);
        verify(caseDispatchService, times(1)).dispatch(sakDto);
        verifyNoMoreInteractions(caseDispatchService);
    }

    @Test
    public void givenCaseTypeBySearchOrNewAndNoCaseFoundAndNewCaseFailedShouldReturnFailedResult() {
        doReturn(CaseDispatchType.BY_SEARCH_OR_NEW).when(archiveInstance).getType();

        SakDto sakDto = mock(SakDto.class);
        doReturn(sakDto).when(archiveInstance).getNewCase();
        doReturn(Optional.of(List.of())).when(sakDto).getJournalpost();

        CaseSearchResult caseSearchResult = mock(CaseSearchResult.class);
        doReturn(ACCEPTED).when(caseSearchResult).getStatus();
        doReturn(List.of()).when(caseSearchResult).getArchiveCaseIds();
        doReturn(Mono.just(caseSearchResult)).when(caseDispatchService).findCasesBySearch(archiveInstance);

        CaseDispatchResult caseDispatchResult = mock(CaseDispatchResult.class);
        doReturn(DispatchStatus.FAILED).when(caseDispatchResult).getStatus();
        doReturn(Mono.just(caseDispatchResult)).when(caseDispatchService).dispatch(sakDto);

        StepVerifier.create(
                        dispatchService.process(instanceFlowHeaders, archiveInstance)
                )
                .expectNext(DispatchResult.failed("Sak dispatch failed"))
                .verifyComplete();

        verify(archiveInstance, times(1)).getType();
        verify(archiveInstance, times(2)).getNewCase();
        verifyNoMoreInteractions(archiveInstance);

        verify(caseDispatchService, times(1)).findCasesBySearch(archiveInstance);
        verify(caseDispatchService, times(1)).dispatch(sakDto);
        verifyNoMoreInteractions(caseDispatchService);
    }

}
