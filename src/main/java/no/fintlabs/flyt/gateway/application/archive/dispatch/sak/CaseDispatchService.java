package no.fintlabs.flyt.gateway.application.archive.dispatch.sak;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.arkiv.noark.SakResource;
import no.fintlabs.flyt.gateway.application.archive.dispatch.mapping.SakMappingService;
import no.fintlabs.flyt.gateway.application.archive.dispatch.model.instance.ArchiveInstance;
import no.fintlabs.flyt.gateway.application.archive.dispatch.model.instance.SakDto;
import no.fintlabs.flyt.gateway.application.archive.dispatch.sak.result.CaseDispatchResult;
import no.fintlabs.flyt.gateway.application.archive.dispatch.web.FintArchiveDispatchClient;
import no.fintlabs.flyt.gateway.application.archive.resource.web.CaseSearchParametersService;
import no.fintlabs.flyt.gateway.application.archive.resource.web.FintArchiveResourceClient;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;

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
                ).onErrorResume(e -> {
                    log.error("Failed to post case", e);
                    return Mono.just(CaseDispatchResult.failed());
                }).doOnNext(result -> log.info("Dispatch result: {}", result.toString()));
    }

    public Mono<List<SakResource>> findCasesBySearch(ArchiveInstance archiveInstance) {
        try {
            String caseFilter = caseSearchParametersService.createFilterQueryParamValue(
                    archiveInstance.getNewCase(),
                    archiveInstance.getCaseSearchParameters()
            );
            return fintArchiveResourceClient.findCasesWithFilter(caseFilter);
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

}
