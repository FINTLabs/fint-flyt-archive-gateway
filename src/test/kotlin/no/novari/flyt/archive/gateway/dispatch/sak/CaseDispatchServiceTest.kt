package no.novari.flyt.archive.gateway.dispatch.sak

import io.netty.handler.timeout.ReadTimeoutException
import no.novari.fint.model.felles.kompleksedatatyper.Identifikator
import no.novari.fint.model.resource.arkiv.noark.SakResource
import no.novari.flyt.archive.gateway.dispatch.mapping.SakMappingService
import no.novari.flyt.archive.gateway.dispatch.model.instance.ArchiveInstance
import no.novari.flyt.archive.gateway.dispatch.model.instance.CaseSearchParametersDto
import no.novari.flyt.archive.gateway.dispatch.model.instance.SakDto
import no.novari.flyt.archive.gateway.dispatch.sak.result.CaseDispatchResult
import no.novari.flyt.archive.gateway.dispatch.sak.result.CaseSearchResult
import no.novari.flyt.archive.gateway.dispatch.web.CreatedLocationPollTimeoutException
import no.novari.flyt.archive.gateway.dispatch.web.FintArchiveDispatchClient
import no.novari.flyt.archive.gateway.resource.web.CaseSearchParametersService
import no.novari.flyt.archive.gateway.resource.web.FintArchiveResourceClient
import no.novari.flyt.archive.gateway.resource.web.exceptions.KlasseOrderOutOfBoundsException
import no.novari.flyt.archive.gateway.resource.web.exceptions.SearchKlasseOrderNotFoundInCaseException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

@ExtendWith(MockitoExtension::class)
class CaseDispatchServiceTest {
    @Mock
    private lateinit var sakMappingService: SakMappingService

    @Mock
    private lateinit var caseSearchParametersService: CaseSearchParametersService

    @Mock
    private lateinit var fintArchiveDispatchClient: FintArchiveDispatchClient

    @Mock
    private lateinit var fintArchiveResourceClient: FintArchiveResourceClient

    @InjectMocks
    private lateinit var caseDispatchService: CaseDispatchService

    @Test
    fun givenAcceptedCaseShouldReturnAcceptedResultWithCaseId() {
        val sakDto = SakDto.builder().build()
        val sakResource: SakResource = mock()
        val sakResourceResult: SakResource = mock()
        val identifikator: Identifikator = mock()
        whenever(sakMappingService.toSakResource(sakDto)).thenReturn(sakResource)
        whenever(sakResourceResult.mappeId).thenReturn(identifikator)
        whenever(identifikator.identifikatorverdi).thenReturn("testArchiveCaseId")
        whenever(fintArchiveDispatchClient.postCase(sakResource)).thenReturn(Mono.just(sakResourceResult))

        StepVerifier
            .create(caseDispatchService.dispatch(sakDto))
            .expectNext(CaseDispatchResult.accepted("testArchiveCaseId"))
            .verifyComplete()
    }

    @Test
    fun givenWebclientResponseExceptionFromPostCaseShouldReturnDeclinedResultWithErrorMessage() {
        val sakDto = SakDto.builder().build()
        val sakResource: SakResource = mock()
        val error: WebClientResponseException = mock()
        whenever(sakMappingService.toSakResource(sakDto)).thenReturn(sakResource)
        whenever(error.responseBodyAsString).thenReturn("test response body")
        whenever(fintArchiveDispatchClient.postCase(sakResource)).thenReturn(Mono.error(error))

        StepVerifier
            .create(caseDispatchService.dispatch(sakDto))
            .expectNext(CaseDispatchResult.declined("test response body"))
            .verifyComplete()
    }

    @Test
    fun givenTimeoutFromPostCaseShouldReturnTimedOutResult() {
        val sakDto = SakDto.builder().build()
        val sakResource: SakResource = mock()
        whenever(sakMappingService.toSakResource(sakDto)).thenReturn(sakResource)
        whenever(fintArchiveDispatchClient.postCase(sakResource)).thenReturn(Mono.error(ReadTimeoutException()))

        StepVerifier
            .create(caseDispatchService.dispatch(sakDto))
            .expectNext(CaseDispatchResult.timedOut())
            .verifyComplete()
    }

    @Test
    fun givenCreatedLocationTimeoutFromPostCaseShouldReturnTimedOutResult() {
        val sakDto = SakDto.builder().build()
        val sakResource: SakResource = mock()
        whenever(sakMappingService.toSakResource(sakDto)).thenReturn(sakResource)
        whenever(
            fintArchiveDispatchClient.postCase(sakResource),
        ).thenReturn(Mono.error(CreatedLocationPollTimeoutException()))

        StepVerifier
            .create(caseDispatchService.dispatch(sakDto))
            .expectNext(CaseDispatchResult.timedOut())
            .verifyComplete()
    }

    @Test
    fun findCasesBySearchShouldCreateFilterAndCallClient() {
        val archiveInstance =
            ArchiveInstance
                .builder()
                .newCase(SakDto.builder().build())
                .caseSearchParameters(CaseSearchParametersDto.builder().build())
                .build()
        whenever(
            caseSearchParametersService.createFilterQueryParamValue(
                archiveInstance.newCase!!,
                archiveInstance.caseSearchParameters!!,
            ),
        ).thenReturn("test case filter")
        whenever(fintArchiveResourceClient.findCasesWithFilter("test case filter")).thenReturn(Mono.just(emptyList()))

        StepVerifier
            .create(caseDispatchService.findCasesBySearch(archiveInstance))
            .expectNext(CaseSearchResult.accepted(emptyList()))
            .verifyComplete()
    }

    @Test
    fun givenKlasseOrderOutOfBoundsExceptionFromSearchParameterServiceShouldReturnDeclinedResultWithMessage() {
        val archiveInstance =
            ArchiveInstance
                .builder()
                .newCase(SakDto.builder().build())
                .caseSearchParameters(CaseSearchParametersDto.builder().build())
                .build()
        whenever(
            caseSearchParametersService.createFilterQueryParamValue(
                archiveInstance.newCase!!,
                archiveInstance.caseSearchParameters!!,
            ),
        ).thenThrow(KlasseOrderOutOfBoundsException(1))

        StepVerifier
            .create(caseDispatchService.findCasesBySearch(archiveInstance))
            .expectNext(CaseSearchResult.declined(KlasseOrderOutOfBoundsException(1).message!!))
            .verifyComplete()
    }

    @Test
    fun givenSearchKlasseOrderNotFoundInCaseExceptionShouldReturnDeclinedResultWithMessage() {
        val archiveInstance =
            ArchiveInstance
                .builder()
                .newCase(SakDto.builder().build())
                .caseSearchParameters(CaseSearchParametersDto.builder().build())
                .build()
        whenever(
            caseSearchParametersService.createFilterQueryParamValue(
                archiveInstance.newCase!!,
                archiveInstance.caseSearchParameters!!,
            ),
        ).thenThrow(SearchKlasseOrderNotFoundInCaseException(listOf(1, 2), 3))

        StepVerifier
            .create(caseDispatchService.findCasesBySearch(archiveInstance))
            .expectNext(CaseSearchResult.declined(SearchKlasseOrderNotFoundInCaseException(listOf(1, 2), 3).message!!))
            .verifyComplete()
    }
}
