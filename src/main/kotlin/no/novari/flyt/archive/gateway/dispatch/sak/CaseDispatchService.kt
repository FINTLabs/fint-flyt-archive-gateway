package no.novari.flyt.archive.gateway.dispatch.sak

import io.github.oshai.kotlinlogging.KotlinLogging
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
        log.info { "Dispatching case" }
        val sakResource: SakResource = sakMappingService.toSakResource(sakDto)

        val result =
            try {
                val resultSak = fintArchiveDispatchClient.postCase(sakResource)
                val archiveCaseId = resultSak.mappeId.identifikatorverdi
                log.atInfo {
                    message = "Successfully posted case with archive case id = {}"
                    arguments = arrayOf(archiveCaseId)
                }
                CaseDispatchResult.accepted(archiveCaseId)
            } catch (error: RestClientResponseException) {
                log.atInfo {
                    message = "Post request for case was declined with message='{}'"
                    arguments = arrayOf(error.responseBodyAsString)
                }
                CaseDispatchResult.declined(error.responseBodyAsString)
            } catch (error: CreatedLocationPollTimeoutException) {
                log.atError {
                    message = "Case dispatch timed out"
                    cause = error
                }
                CaseDispatchResult.timedOut()
            } catch (error: Throwable) {
                if (isReadTimeout(error)) {
                    log.atError {
                        message = "Case dispatch timed out"
                        cause = error
                    }
                    CaseDispatchResult.timedOut()
                } else {
                    log.atError {
                        message = "Failed to post case"
                        cause = error
                    }
                    CaseDispatchResult.failed()
                }
            }
        log.atInfo {
            message = "Dispatch result: {}"
            arguments = arrayOf(result)
        }
        return result
    }

    fun findCasesBySearch(archiveInstance: ArchiveInstance): CaseSearchResult {
        log.info { "Searching for cases" }

        val caseFilter =
            try {
                val newCase = requireNotNull(archiveInstance.newCase)
                val caseSearchParameters = requireNotNull(archiveInstance.caseSearchParameters)
                caseSearchParametersService.createFilterQueryParamValue(newCase, caseSearchParameters)
            } catch (error: SearchKlasseOrderNotFoundInCaseException) {
                log.atError {
                    message = "Case search failed"
                    cause = error
                }
                return CaseSearchResult.declined(error.message.orEmpty())
            } catch (error: KlasseOrderOutOfBoundsException) {
                log.atError {
                    message = "Case search failed"
                    cause = error
                }
                return CaseSearchResult.declined(error.message.orEmpty())
            } catch (error: Exception) {
                log.atError {
                    message = "Case search failed"
                    cause = error
                }
                return CaseSearchResult.failed()
            }
        log.atDebug {
            message = "Generated case filter: {}"
            arguments = arrayOf(caseFilter)
        }

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
                    log.atError {
                        message = "Case search timed out"
                        cause = error
                    }
                    CaseSearchResult.timedOut()
                } else {
                    log.atError {
                        message = "Case search failed"
                        cause = error
                    }
                    CaseSearchResult.failed()
                }
            }
        log.atInfo {
            message = "Search result: {}"
            arguments = arrayOf(result)
        }
        return result
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}
