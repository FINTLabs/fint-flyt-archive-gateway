package no.fintlabs.flyt.gateway.application.archive.dispatch;

import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.arkiv.noark.SakResource;
import no.fintlabs.flyt.gateway.application.archive.dispatch.model.CaseDispatchType;
import no.fintlabs.flyt.gateway.application.archive.dispatch.model.instance.ArchiveInstance;
import no.fintlabs.flyt.gateway.application.archive.dispatch.model.instance.JournalpostDto;
import no.fintlabs.flyt.gateway.application.archive.dispatch.model.instance.SakDto;
import no.fintlabs.flyt.gateway.application.archive.dispatch.sak.CaseDispatchService;
import no.fintlabs.flyt.gateway.application.archive.dispatch.sak.result.CaseDispatchResult;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

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
    public void givenCaseTypeBySearchOrNewWithNoJournalPostShouldReturnDeclinedResultWithErrorMessage() {
        doReturn(CaseDispatchType.BY_SEARCH_OR_NEW).when(archiveInstance).getType();

        SakDto sakDto = mock(SakDto.class);
        doReturn(sakDto).when(archiveInstance).getNewCase();
        doReturn(Optional.of(List.of())).when(sakDto).getJournalpost();

        StepVerifier.create(
                        dispatchService.process(instanceFlowHeaders, archiveInstance)
                )
                .expectNext(DispatchResult.declined("Instance contains no records"))
                .verifyComplete();

        verify(archiveInstance, times(1)).getType();
        verify(archiveInstance, times(1)).getNewCase();
        verifyNoMoreInteractions(archiveInstance);
    }

    @Test
    public void givenCaseTypeBySearchOrNewWithJournalPostShouldReturnDeclinedResultWhenMultipleCasesAreFound() {
        doReturn(CaseDispatchType.BY_SEARCH_OR_NEW).when(archiveInstance).getType();

        SakDto sakDto = mock(SakDto.class);
        doReturn(sakDto).when(archiveInstance).getNewCase();

        JournalpostDto journalpostDto = mock(JournalpostDto.class);
        doReturn(Optional.of(List.of(journalpostDto))).when(sakDto).getJournalpost();

        SakResource sakResource1 = mock(SakResource.class);
        SakResource sakResource2 = mock(SakResource.class);
        doReturn(Mono.just(List.of(sakResource1, sakResource2))).when(caseDispatchService)
                .findCasesBySearch(archiveInstance);

        StepVerifier.create(
                        dispatchService.process(instanceFlowHeaders, archiveInstance)
                )
                .expectNext(DispatchResult.declined("Found multiple cases"))
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

        SakResource sakResource = mock(SakResource.class);
        doReturn(Mono.just(List.of(sakResource))).when(caseDispatchService).findCasesBySearch(archiveInstance);
        Identifikator identifikator = mock(Identifikator.class);
        doReturn(identifikator).when(sakResource).getMappeId();
        doReturn("testCaseId").when(identifikator).getIdentifikatorverdi();

        doReturn(Mono.just(DispatchResult.accepted("testCaseId")))
                .when(recordsProcessingService)
                .processRecords("testCaseId", false, List.of(journalpostDto));

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

        doReturn(Mono.just(List.of())).when(caseDispatchService).findCasesBySearch(archiveInstance);
        doReturn(Mono.just(CaseDispatchResult.accepted("testCaseId"))).when(caseDispatchService)
                .dispatch(sakDto);

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

        verify(caseDispatchService, times(1)).findCasesBySearch(archiveInstance);
        verifyNoMoreInteractions(caseDispatchService);

        verify(recordsProcessingService, times(1))
                .processRecords("testCaseId", true, List.of(journalpostDto));
        verifyNoMoreInteractions(recordsProcessingService);
    }


}