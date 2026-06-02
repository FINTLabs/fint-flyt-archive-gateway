package no.novari.flyt.archive.gateway.dispatch.sak

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
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.ResourceAccessException
import java.net.http.HttpTimeoutException

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
    fun `given an accepted case, returns an accepted result with case id`() {
        val sakDto = SakDto.builder().build()
        val sakResource: SakResource = mock()
        val sakResourceResult: SakResource = mock()
        val identifikator: Identifikator = mock()
        whenever(sakMappingService.toSakResource(sakDto)).thenReturn(sakResource)
        whenever(sakResourceResult.mappeId).thenReturn(identifikator)
        whenever(identifikator.identifikatorverdi).thenReturn("testArchiveCaseId")
        whenever(fintArchiveDispatchClient.postCase(sakResource)).thenReturn(sakResourceResult)

        val result = caseDispatchService.dispatch(sakDto)

        assertThat(result).isEqualTo(CaseDispatchResult.accepted("testArchiveCaseId"))
    }

    @Test
    fun `given a RestClientResponseException from postCase, returns a declined result with error message`() {
        val sakDto = SakDto.builder().build()
        val sakResource: SakResource = mock()
        val error: HttpClientErrorException = mock()
        whenever(sakMappingService.toSakResource(sakDto)).thenReturn(sakResource)
        whenever(error.responseBodyAsString).thenReturn("test response body")
        whenever(fintArchiveDispatchClient.postCase(sakResource)).thenThrow(error)

        val result = caseDispatchService.dispatch(sakDto)

        assertThat(result).isEqualTo(CaseDispatchResult.declined("test response body"))
    }

    @Test
    fun `given a read timeout from postCase, returns a timed-out result`() {
        val sakDto = SakDto.builder().build()
        val sakResource: SakResource = mock()
        whenever(sakMappingService.toSakResource(sakDto)).thenReturn(sakResource)
        whenever(fintArchiveDispatchClient.postCase(sakResource))
            .thenThrow(ResourceAccessException("read timeout", HttpTimeoutException("read timeout")))

        val result = caseDispatchService.dispatch(sakDto)

        assertThat(result).isEqualTo(CaseDispatchResult.timedOut())
    }

    @Test
    fun `given a CreatedLocationPollTimeoutException from postCase, returns a timed-out result`() {
        val sakDto = SakDto.builder().build()
        val sakResource: SakResource = mock()
        whenever(sakMappingService.toSakResource(sakDto)).thenReturn(sakResource)
        whenever(fintArchiveDispatchClient.postCase(sakResource))
            .thenThrow(CreatedLocationPollTimeoutException())

        val result = caseDispatchService.dispatch(sakDto)

        assertThat(result).isEqualTo(CaseDispatchResult.timedOut())
    }

    @Test
    fun `findCasesBySearch creates a filter and calls the client`() {
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
        whenever(fintArchiveResourceClient.findCasesWithFilter("test case filter")).thenReturn(emptyList())

        val result = caseDispatchService.findCasesBySearch(archiveInstance)

        assertThat(result).isEqualTo(CaseSearchResult.accepted(emptyList()))
    }

    @Test
    fun `given a KlasseOrderOutOfBoundsException, returns a declined result with message`() {
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

        val result = caseDispatchService.findCasesBySearch(archiveInstance)

        assertThat(result).isEqualTo(CaseSearchResult.declined(KlasseOrderOutOfBoundsException(1).message!!))
    }

    @Test
    fun `given a SearchKlasseOrderNotFoundInCaseException, returns a declined result with message`() {
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

        val result = caseDispatchService.findCasesBySearch(archiveInstance)

        assertThat(result)
            .isEqualTo(CaseSearchResult.declined(SearchKlasseOrderNotFoundInCaseException(listOf(1, 2), 3).message!!))
    }
}
