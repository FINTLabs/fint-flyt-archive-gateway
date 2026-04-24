package no.novari.flyt.archive.gateway.dispatch.sak

import io.netty.handler.timeout.ReadTimeoutException
import no.novari.fint.model.felles.kompleksedatatyper.Identifikator
import no.novari.fint.model.resource.arkiv.noark.SakResource
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
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono

@Service
class CaseDispatchService(
    private val sakMappingService: SakMappingService,
    private val caseSearchParametersService: CaseSearchParametersService,
    private val fintArchiveDispatchClient: FintArchiveDispatchClient,
    private val fintArchiveResourceClient: FintArchiveResourceClient,
) {
    fun dispatch(sakDto: SakDto): Mono<CaseDispatchResult> {
        log.info("Dispatching case")
        val sakResource: SakResource = sakMappingService.toSakResource(sakDto)
        return fintArchiveDispatchClient
            .postCase(sakResource)
            .map { CaseDispatchResult.accepted(it.mappeId.identifikatorverdi) }
            .doOnNext { caseDispatchResult ->
                log.info(
                    "Successfully posted case with archive case id = {}",
                    caseDispatchResult.archiveCaseId,
                )
            }.onErrorResume(WebClientResponseException::class.java) { error ->
                log.info("Post request for case was declined with message='{}'", error.responseBodyAsString)
                Mono.just(CaseDispatchResult.declined(error.responseBodyAsString))
            }.onErrorResume({ error ->
                error is ReadTimeoutException || error is CreatedLocationPollTimeoutException
            }) {
                log.error("Case dispatch timed out", it)
                Mono.just(CaseDispatchResult.timedOut())
            }.onErrorResume {
                log.error("Failed to post case", it)
                Mono.just(CaseDispatchResult.failed())
            }.doOnNext { result -> log.info("Dispatch result: {}", result) }
    }

    fun findCasesBySearch(archiveInstance: ArchiveInstance): Mono<CaseSearchResult> {
        log.info("Searching for cases")

        return try {
            val newCase = requireNotNull(archiveInstance.newCase)
            val caseSearchParameters = requireNotNull(archiveInstance.caseSearchParameters)
            val caseFilter =
                caseSearchParametersService.createFilterQueryParamValue(
                    newCase,
                    caseSearchParameters,
                )
            log.debug("Generated case filter: {}", caseFilter)

            fintArchiveResourceClient
                .findCasesWithFilter(caseFilter)
                .map { sakResources ->
                    sakResources
                        .map(SakResource::getMappeId)
                        .map(Identifikator::getIdentifikatorverdi)
                }.map(CaseSearchResult::accepted)
                .onErrorResume(ReadTimeoutException::class.java) {
                    log.error("Case search timed out", it)
                    Mono.just(CaseSearchResult.timedOut())
                }.onErrorResume(Exception::class.java) {
                    log.error("Case search failed", it)
                    Mono.just(CaseSearchResult.failed())
                }.doOnNext { result -> log.info("Search result: {}", result) }
        } catch (error: SearchKlasseOrderNotFoundInCaseException) {
            log.error("Case search failed", error)
            Mono.just(CaseSearchResult.declined(error.message.orEmpty()))
        } catch (error: KlasseOrderOutOfBoundsException) {
            log.error("Case search failed", error)
            Mono.just(CaseSearchResult.declined(error.message.orEmpty()))
        } catch (error: Exception) {
            log.error("Case search failed", error)
            Mono.just(CaseSearchResult.failed())
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(CaseDispatchService::class.java)
    }
}
