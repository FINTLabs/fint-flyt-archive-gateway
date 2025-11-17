package no.novari.flyt.archive.gateway.dispatch.sak;

import io.netty.handler.timeout.ReadTimeoutException;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.arkiv.noark.SakResource;
import no.novari.flyt.archive.gateway.dispatch.mapping.SakMappingService;
import no.novari.flyt.archive.gateway.dispatch.model.instance.ArchiveInstance;
import no.novari.flyt.archive.gateway.dispatch.model.instance.CaseSearchParametersDto;
import no.novari.flyt.archive.gateway.dispatch.model.instance.SakDto;
import no.novari.flyt.archive.gateway.dispatch.sak.result.CaseDispatchResult;
import no.novari.flyt.archive.gateway.dispatch.sak.result.CaseSearchResult;
import no.novari.flyt.archive.gateway.dispatch.web.CreatedLocationPollTimeoutException;
import no.novari.flyt.archive.gateway.dispatch.web.FintArchiveDispatchClient;
import no.novari.flyt.archive.gateway.resource.web.CaseSearchParametersService;
import no.novari.flyt.archive.gateway.resource.web.FintArchiveResourceClient;
import no.novari.flyt.archive.gateway.resource.web.exceptions.KlasseOrderOutOfBoundsException;
import no.novari.flyt.archive.gateway.resource.web.exceptions.SearchKlasseOrderNotFoundInCaseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class CaseDispatchServiceTest {

    @Mock
    private SakMappingService sakMappingService;
    @Mock
    private CaseSearchParametersService caseSearchParametersService;
    @Mock
    private FintArchiveDispatchClient fintArchiveDispatchClient;
    @Mock
    private FintArchiveResourceClient fintArchiveResourceClient;
    @InjectMocks
    private CaseDispatchService caseDispatchService;

    @Test
    public void givenAcceptedCaseShouldReturnAcceptedResultWithCaseId() {
        SakDto sakDto = mock(SakDto.class);
        SakResource sakResource = mock(SakResource.class);
        doReturn(sakResource).when(sakMappingService).toSakResource(sakDto);

        SakResource sakResourceResult = mock(SakResource.class);
        Identifikator identifikator = mock(Identifikator.class);
        doReturn(identifikator).when(sakResourceResult).getMappeId();
        doReturn("testArchiveCaseId").when(identifikator).getIdentifikatorverdi();
        doReturn(Mono.just(sakResourceResult)).when(fintArchiveDispatchClient).postCase(sakResource);

        StepVerifier.create(
                        caseDispatchService.dispatch(sakDto)
                )
                .expectNext(CaseDispatchResult.accepted("testArchiveCaseId"))
                .verifyComplete();

        verify(sakMappingService, times(1)).toSakResource(sakDto);
        verifyNoMoreInteractions(sakMappingService);

        verify(fintArchiveDispatchClient, times(1)).postCase(sakResource);
        verifyNoMoreInteractions(fintArchiveDispatchClient);
    }

    @Test
    public void givenWebclientResponseExceptionFromPostCaseShouldReturnDeclinedResultWithErrorMessage() {
        SakDto sakDto = mock(SakDto.class);
        SakResource sakResource = mock(SakResource.class);
        doReturn(sakResource).when(sakMappingService).toSakResource(sakDto);

        WebClientResponseException webClientResponseException = mock(WebClientResponseException.class);
        doReturn("test response body").when(webClientResponseException).getResponseBodyAsString();
        doReturn(Mono.error(webClientResponseException)).when(fintArchiveDispatchClient).postCase(sakResource);

        StepVerifier.create(
                        caseDispatchService.dispatch(sakDto)
                )
                .expectNext(CaseDispatchResult.declined("test response body"))
                .verifyComplete();

        verify(sakMappingService, times(1)).toSakResource(sakDto);
        verifyNoMoreInteractions(sakMappingService);

        verify(fintArchiveDispatchClient, times(1)).postCase(sakResource);
        verifyNoMoreInteractions(fintArchiveDispatchClient);
    }

    @Test
    public void givenReadTimeoutExceptionFromPostCaseShouldReturnFailedTimedOutResult() {
        SakDto sakDto = mock(SakDto.class);
        SakResource sakResource = mock(SakResource.class);
        doReturn(sakResource).when(sakMappingService).toSakResource(sakDto);

        doReturn(Mono.error(new ReadTimeoutException())).when(fintArchiveDispatchClient).postCase(sakResource);

        StepVerifier.create(
                        caseDispatchService.dispatch(sakDto)
                )
                .expectNext(CaseDispatchResult.timedOut())
                .verifyComplete();

        verify(sakMappingService, times(1)).toSakResource(sakDto);
        verifyNoMoreInteractions(sakMappingService);

        verify(fintArchiveDispatchClient, times(1)).postCase(sakResource);
        verifyNoMoreInteractions(fintArchiveDispatchClient);
    }

    @Test
    public void givenCreatedLocationPollTimeoutExceptionFromPostCaseShouldReturnFailedTimedOutResult() {
        SakDto sakDto = mock(SakDto.class);
        SakResource sakResource = mock(SakResource.class);
        doReturn(sakResource).when(sakMappingService).toSakResource(sakDto);

        doReturn(Mono.error(new CreatedLocationPollTimeoutException())).when(fintArchiveDispatchClient).postCase(sakResource);

        StepVerifier.create(
                        caseDispatchService.dispatch(sakDto)
                )
                .expectNext(CaseDispatchResult.timedOut())
                .verifyComplete();

        verify(sakMappingService, times(1)).toSakResource(sakDto);
        verifyNoMoreInteractions(sakMappingService);

        verify(fintArchiveDispatchClient, times(1)).postCase(sakResource);
        verifyNoMoreInteractions(fintArchiveDispatchClient);
    }

    @Test
    public void givenExceptionOtherThanWebclientResponseExceptionAndTimeoutExceptionFromPostCaseShouldReturnFailedResult() {
        SakDto sakDto = mock(SakDto.class);
        SakResource sakResource = mock(SakResource.class);
        doReturn(sakResource).when(sakMappingService).toSakResource(sakDto);
        doReturn(Mono.error(new RuntimeException())).when(fintArchiveDispatchClient).postCase(sakResource);

        StepVerifier.create(
                        caseDispatchService.dispatch(sakDto)
                )
                .expectNext(CaseDispatchResult.failed())
                .verifyComplete();

        verify(sakMappingService, times(1)).toSakResource(sakDto);
        verifyNoMoreInteractions(sakMappingService);

        verify(fintArchiveDispatchClient, times(1)).postCase(sakResource);
        verifyNoMoreInteractions(fintArchiveDispatchClient);
    }

    @Test
    public void findCasesBySearchShouldCreateFilterWithCaseAndSearchParametersAndCallFindSingleCaseWithFilter() {
        ArchiveInstance archiveInstance = mock(ArchiveInstance.class);

        SakDto sakDto = mock(SakDto.class);
        doReturn(sakDto).when(archiveInstance).getNewCase();

        CaseSearchParametersDto caseSearchParametersDto = mock(CaseSearchParametersDto.class);
        doReturn(caseSearchParametersDto).when(archiveInstance).getCaseSearchParameters();

        doReturn("test case filter").when(caseSearchParametersService).createFilterQueryParamValue(
                sakDto,
                caseSearchParametersDto
        );

        SakResource sakResource = mock(SakResource.class);
        doReturn(Mono.just(List.of(sakResource))).when(fintArchiveResourceClient).findCasesWithFilter("test case filter");

        StepVerifier.create(
                        caseDispatchService.findCasesBySearch(archiveInstance)
                )
                .expectNextCount(1L)
                .verifyComplete();

        verify(caseSearchParametersService, times(1)).createFilterQueryParamValue(sakDto, caseSearchParametersDto);
        verifyNoMoreInteractions(caseSearchParametersService);

        verify(fintArchiveResourceClient, times(1)).findCasesWithFilter("test case filter");
        verifyNoMoreInteractions(fintArchiveResourceClient);
    }

    @Test
    public void givenKlasseOrderOutOfBoundsExceptionFromSearchParameterServiceFindCasesBySearchShouldReturnDeclinedResultWithMessage() {
        ArchiveInstance archiveInstance = mock(ArchiveInstance.class);

        SakDto sakDto = mock(SakDto.class);
        doReturn(sakDto).when(archiveInstance).getNewCase();

        CaseSearchParametersDto caseSearchParametersDto = mock(CaseSearchParametersDto.class);
        doReturn(caseSearchParametersDto).when(archiveInstance).getCaseSearchParameters();

        doThrow(new KlasseOrderOutOfBoundsException(1)).when(caseSearchParametersService).createFilterQueryParamValue(
                sakDto,
                caseSearchParametersDto
        );

        StepVerifier.create(
                        caseDispatchService.findCasesBySearch(archiveInstance)
                )
                .expectNext(CaseSearchResult.declined(new KlasseOrderOutOfBoundsException(1).getMessage()))
                .verifyComplete();

        verify(caseSearchParametersService, times(1)).createFilterQueryParamValue(sakDto, caseSearchParametersDto);
        verifyNoMoreInteractions(caseSearchParametersService);
    }

    @Test
    public void givenSearchKlasseOrderNotFoundInCaseExceptionFromSearchParameterServiceFindCasesBySearchShouldReturnDeclinedResultWithMessage() {
        ArchiveInstance archiveInstance = mock(ArchiveInstance.class);

        SakDto sakDto = mock(SakDto.class);
        doReturn(sakDto).when(archiveInstance).getNewCase();

        CaseSearchParametersDto caseSearchParametersDto = mock(CaseSearchParametersDto.class);
        doReturn(caseSearchParametersDto).when(archiveInstance).getCaseSearchParameters();

        doThrow(new SearchKlasseOrderNotFoundInCaseException(List.of(1, 2), 3))
                .when(caseSearchParametersService).createFilterQueryParamValue(
                        sakDto,
                        caseSearchParametersDto
                );

        StepVerifier.create(
                        caseDispatchService.findCasesBySearch(archiveInstance)
                )
                .expectNext(CaseSearchResult.declined(
                        new SearchKlasseOrderNotFoundInCaseException(List.of(1, 2), 3)
                                .getMessage()))
                .verifyComplete();

        verify(caseSearchParametersService, times(1)).createFilterQueryParamValue(sakDto, caseSearchParametersDto);
        verifyNoMoreInteractions(caseSearchParametersService);
    }

    @Test
    public void givenExceptionOtherThanSpecificallyHandledExceptionsFromSearchParameterServiceFindCasesBySearchShouldReturnFailedResult() {
        ArchiveInstance archiveInstance = mock(ArchiveInstance.class);

        SakDto sakDto = mock(SakDto.class);
        doReturn(sakDto).when(archiveInstance).getNewCase();

        CaseSearchParametersDto caseSearchParametersDto = mock(CaseSearchParametersDto.class);
        doReturn(caseSearchParametersDto).when(archiveInstance).getCaseSearchParameters();

        doThrow(new RuntimeException())
                .when(caseSearchParametersService).createFilterQueryParamValue(
                        sakDto,
                        caseSearchParametersDto
                );

        StepVerifier.create(
                        caseDispatchService.findCasesBySearch(archiveInstance)
                )
                .expectNext(CaseSearchResult.failed())
                .verifyComplete();

        verify(caseSearchParametersService, times(1)).createFilterQueryParamValue(sakDto, caseSearchParametersDto);
        verifyNoMoreInteractions(caseSearchParametersService);
    }

    @Test
    public void givenSingleCaseReturnedFromArchiveClientFindCasesBySearchShouldReturnAcceptedResultWithCaseId() {
        ArchiveInstance archiveInstance = mock(ArchiveInstance.class);

        SakDto sakDto = mock(SakDto.class);
        doReturn(sakDto).when(archiveInstance).getNewCase();

        CaseSearchParametersDto caseSearchParametersDto = mock(CaseSearchParametersDto.class);
        doReturn(caseSearchParametersDto).when(archiveInstance).getCaseSearchParameters();

        doReturn("test case filter").when(caseSearchParametersService).createFilterQueryParamValue(
                sakDto,
                caseSearchParametersDto
        );

        Identifikator identifikator = mock(Identifikator.class);
        doReturn("caseId1").when(identifikator).getIdentifikatorverdi();
        SakResource sakResource = mock(SakResource.class);
        doReturn(identifikator).when(sakResource).getMappeId();

        doReturn(Mono.just(List.of(sakResource))).when(fintArchiveResourceClient).findCasesWithFilter("test case filter");

        StepVerifier.create(
                        caseDispatchService.findCasesBySearch(archiveInstance)
                )
                .expectNext(CaseSearchResult.accepted(List.of("caseId1")))
                .verifyComplete();
    }

    @Test
    public void givenMultipleCasesReturnedFromArchiveClientFindCasesBySearchShouldReturnAcceptedResultWithCaseIds() {
        ArchiveInstance archiveInstance = mock(ArchiveInstance.class);

        SakDto sakDto = mock(SakDto.class);
        doReturn(sakDto).when(archiveInstance).getNewCase();

        CaseSearchParametersDto caseSearchParametersDto = mock(CaseSearchParametersDto.class);
        doReturn(caseSearchParametersDto).when(archiveInstance).getCaseSearchParameters();

        doReturn("test case filter").when(caseSearchParametersService).createFilterQueryParamValue(
                sakDto,
                caseSearchParametersDto
        );

        Identifikator identifikator1 = mock(Identifikator.class);
        doReturn("caseId1").when(identifikator1).getIdentifikatorverdi();
        SakResource sakResource1 = mock(SakResource.class);
        doReturn(identifikator1).when(sakResource1).getMappeId();

        Identifikator identifikator2 = mock(Identifikator.class);
        doReturn("caseId2").when(identifikator2).getIdentifikatorverdi();
        SakResource sakResource2 = mock(SakResource.class);
        doReturn(identifikator2).when(sakResource2).getMappeId();

        doReturn(Mono.just(List.of(sakResource1, sakResource2)))
                .when(fintArchiveResourceClient).findCasesWithFilter("test case filter");


        StepVerifier.create(
                        caseDispatchService.findCasesBySearch(archiveInstance)
                )
                .expectNext(CaseSearchResult.accepted(List.of("caseId1", "caseId2")))
                .verifyComplete();
    }

    @Test
    public void givenNoCasesReturnedFromArchiveClientFindCasesBySearchShouldReturnAcceptedResultWithNoCaseIds() {
        ArchiveInstance archiveInstance = mock(ArchiveInstance.class);

        SakDto sakDto = mock(SakDto.class);
        doReturn(sakDto).when(archiveInstance).getNewCase();

        CaseSearchParametersDto caseSearchParametersDto = mock(CaseSearchParametersDto.class);
        doReturn(caseSearchParametersDto).when(archiveInstance).getCaseSearchParameters();

        doReturn("test case filter").when(caseSearchParametersService).createFilterQueryParamValue(
                sakDto,
                caseSearchParametersDto
        );

        doReturn(Mono.just(List.of()))
                .when(fintArchiveResourceClient).findCasesWithFilter("test case filter");

        StepVerifier.create(
                        caseDispatchService.findCasesBySearch(archiveInstance)
                )
                .expectNext(CaseSearchResult.accepted(List.of()))
                .verifyComplete();
    }

    @Test
    public void givenExceptionFromFintArchiveResourceClientFindCasesBySearchShouldReturnFailedResult() {
        ArchiveInstance archiveInstance = mock(ArchiveInstance.class);

        SakDto sakDto = mock(SakDto.class);
        doReturn(sakDto).when(archiveInstance).getNewCase();

        CaseSearchParametersDto caseSearchParametersDto = mock(CaseSearchParametersDto.class);
        doReturn(caseSearchParametersDto).when(archiveInstance).getCaseSearchParameters();

        doReturn("test case filter").when(caseSearchParametersService).createFilterQueryParamValue(
                sakDto,
                caseSearchParametersDto
        );

        doThrow(new RuntimeException())
                .when(fintArchiveResourceClient).findCasesWithFilter("test case filter");

        StepVerifier.create(
                        caseDispatchService.findCasesBySearch(archiveInstance)
                )
                .expectNext(CaseSearchResult.failed())
                .verifyComplete();
    }

}