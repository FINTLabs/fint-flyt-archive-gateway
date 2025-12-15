package no.novari.flyt.archive.gateway.dispatch.sak;

import io.netty.handler.timeout.ReadTimeoutException;
import lombok.extern.slf4j.Slf4j;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.arkiv.noark.SakResource;
import no.novari.flyt.archive.gateway.dispatch.mapping.SakMappingService;
import no.novari.flyt.archive.gateway.dispatch.model.instance.ArchiveInstance;
import no.novari.flyt.archive.gateway.dispatch.model.instance.SakDto;
import no.novari.flyt.archive.gateway.dispatch.sak.result.CaseDispatchResult;
import no.novari.flyt.archive.gateway.dispatch.sak.result.CaseSearchResult;
import no.novari.flyt.archive.gateway.dispatch.web.CreatedLocationPollTimeoutException;
import no.novari.flyt.archive.gateway.dispatch.web.FintArchiveDispatchClient;
import no.novari.flyt.archive.gateway.resource.web.CaseSearchParametersService;
import no.novari.flyt.archive.gateway.resource.web.FintArchiveResourceClient;
import no.novari.flyt.archive.gateway.resource.web.exceptions.KlasseOrderOutOfBoundsException;
import no.novari.flyt.archive.gateway.resource.web.exceptions.SearchKlasseOrderNotFoundInCaseException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class CaseDispatchService {
    private final SakMappingService sakMappingService;
    private final CaseSearchParametersService caseSearchParametersService;
    private final FintArchiveDispatchClient fintArchiveDispatchClient;
    private final FintArchiveResourceClient fintArchiveResourceClient;

    public CaseDispatchService(
            SakMappingService sakMappingService,
            CaseSearchParametersService caseSearchParametersService,
            FintArchiveDispatchClient fintArchiveDispatchClient, FintArchiveResourceClient fintArchiveResourceClient
    ) {
        this.sakMappingService = sakMappingService;
        this.caseSearchParametersService = caseSearchParametersService;
        this.fintArchiveDispatchClient = fintArchiveDispatchClient;
        this.fintArchiveResourceClient = fintArchiveResourceClient;
    }

    public Mono<CaseDispatchResult> dispatch(SakDto sakDto) {
        log.info("Dispatching case");
        SakResource sakResource = sakMappingService.toSakResource(sakDto);
        return fintArchiveDispatchClient.postCase(sakResource)
                .map(sr -> CaseDispatchResult.accepted(sr.getMappeId().getIdentifikatorverdi()))
                .doOnNext(caseDispatchResult -> log.info(
                        "Successfully posted case with archive case id = {}", caseDispatchResult.getArchiveCaseId()
                ))
                .onErrorResume(WebClientResponseException.class, e -> {
                            log.info("Post request for case was declined with message='{}'", e.getResponseBodyAsString());
                            return Mono.just(CaseDispatchResult.declined(e.getResponseBodyAsString()));
                        }
                ).onErrorResume(
                        e -> e instanceof ReadTimeoutException || e instanceof CreatedLocationPollTimeoutException,
                        e -> {
                            log.error("Case dispatch timed out");
                            return Mono.just(CaseDispatchResult.timedOut());
                        }
                )
                .onErrorResume(e -> {
                    log.error("Failed to post case", e);
                    return Mono.just(CaseDispatchResult.failed());
                }).doOnNext(result -> log.info("Dispatch result: {}", result.toString()));
    }

    public Mono<CaseSearchResult> findCasesBySearch(ArchiveInstance archiveInstance) {
        log.info("Searching for cases");
        try {
            String caseFilter = caseSearchParametersService.createFilterQueryParamValue(
                    archiveInstance.getNewCase(),
                    archiveInstance.getCaseSearchParameters()
            );
            log.debug("Generated case filter: {}", caseFilter);

            return fintArchiveResourceClient.findCasesWithFilter(caseFilter)
                    .map(sakResources -> sakResources.stream()
                            .map(SakResource::getMappeId)
                            .map(Identifikator::getIdentifikatorverdi)
                            .toList()
                    )
                    .map(CaseSearchResult::accepted)
                    .onErrorResume(ReadTimeoutException.class, e -> {
                        log.error("Case search timed out");
                        return Mono.just(CaseSearchResult.timedOut());
                    })
                    .onErrorResume(Exception.class, e -> {
                        log.error("Case search failed", e);
                        return Mono.just(CaseSearchResult.failed());
                    }).doOnNext(result -> log.info("Search result: {}", result.toString()));
        } catch (SearchKlasseOrderNotFoundInCaseException | KlasseOrderOutOfBoundsException e) {
            log.error("Case search failed", e);
            return Mono.just(CaseSearchResult.declined(e.getMessage()));
        } catch (Exception e) {
            log.error("Case search failed", e);
            return Mono.just(CaseSearchResult.failed());
        }

    }

}
