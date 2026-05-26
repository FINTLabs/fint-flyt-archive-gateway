package no.novari.flyt.archive.gateway.dispatch.sak

import no.novari.fint.model.felles.kompleksedatatyper.Identifikator
import no.novari.fint.model.resource.arkiv.noark.SakResource
import no.novari.flyt.archive.gateway.dispatch.isReadTimeout
import no.novari.flyt.archive.gateway.dispatch.mapping.SakMappingService
import no.novari.flyt.archive.gateway.dispatch.model.instance.ArchiveInstance
import no.novari.flyt.archive.gateway.dispatch.model.instance.SakDto
import no.novari.flyt.archive.gateway.dispatch.sak.result.CaseDispatchResult
import no.novari.flyt.archive.gateway.dispatch.sak.result.CaseSearchResult
import no.novari.flyt.archive.gateway.dispatch.web.CreatedLocationPollTimeoutException
import no.novari.flyt.archive.gateway.dispatch.web.FintArchiveDispatchClient
import no.novari.flyt.archive.gateway.resource.web.CaseSearchParametersService
import no.novari.flyt.archive.gateway.resource.web.FintArchiveResourceClient
import no.novari.flyt.archive.gateway.resource.web.exceptions.KlasseOrderOutOfBoundsException
import no.novari.flyt.archive.gateway.resource.web.exceptions.SearchKlasseOrderNotFoundInCaseException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientResponseException

@Service
class CaseDispatchService(
    private val sakMappingService: SakMappingService,
    private val caseSearchParametersService: CaseSearchParametersService,
    private val fintArchiveDispatchClient: FintArchiveDispatchClient,
    private val fintArchiveResourceClient: FintArchiveResourceClient,
) {
    fun dispatch(sakDto: SakDto): CaseDispatchResult {
        log.info("Dispatching case")
        val sakResource: SakResource = sakMappingService.toSakResource(sakDto)

        val result =
            try {
                val resultSak = fintArchiveDispatchClient.postCase(sakResource)
                val archiveCaseId = resultSak.mappeId.identifikatorverdi
                log.info("Successfully posted case with archive case id = {}", archiveCaseId)
                CaseDispatchResult.accepted(archiveCaseId)
            } catch (error: RestClientResponseException) {
                log.info("Post request for case was declined with message='{}'", error.responseBodyAsString)
                CaseDispatchResult.declined(error.responseBodyAsString)
            } catch (error: CreatedLocationPollTimeoutException) {
                log.error("Case dispatch timed out", error)
                CaseDispatchResult.timedOut()
            } catch (error: Throwable) {
                if (isReadTimeout(error)) {
                    log.error("Case dispatch timed out", error)
                    CaseDispatchResult.timedOut()
                } else {
                    log.error("Failed to post case", error)
                    CaseDispatchResult.failed()
                }
            }
        log.info("Dispatch result: {}", result)
        return result
    }

    fun findCasesBySearch(archiveInstance: ArchiveInstance): CaseSearchResult {
        log.info("Searching for cases")

        val caseFilter =
            try {
                val newCase = requireNotNull(archiveInstance.newCase)
                val caseSearchParameters = requireNotNull(archiveInstance.caseSearchParameters)
                caseSearchParametersService.createFilterQueryParamValue(newCase, caseSearchParameters)
            } catch (error: SearchKlasseOrderNotFoundInCaseException) {
                log.error("Case search failed", error)
                return CaseSearchResult.declined(error.message.orEmpty())
            } catch (error: KlasseOrderOutOfBoundsException) {
                log.error("Case search failed", error)
                return CaseSearchResult.declined(error.message.orEmpty())
            } catch (error: Exception) {
                log.error("Case search failed", error)
                return CaseSearchResult.failed()
            }
        log.debug("Generated case filter: {}", caseFilter)

        val result =
            try {
                val sakResources = fintArchiveResourceClient.findCasesWithFilter(caseFilter)
                val ids =
                    sakResources
                        .map(SakResource::getMappeId)
                        .map(Identifikator::getIdentifikatorverdi)
                CaseSearchResult.accepted(ids)
            } catch (error: Throwable) {
                if (isReadTimeout(error)) {
                    log.error("Case search timed out", error)
                    CaseSearchResult.timedOut()
                } else {
                    log.error("Case search failed", error)
                    CaseSearchResult.failed()
                }
            }
        log.info("Search result: {}", result)
        return result
    }

    companion object {
        private val log = LoggerFactory.getLogger(CaseDispatchService::class.java)
    }
}
