package no.fintlabs.dispatch.sak;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.arkiv.noark.SakResource;
import no.fintlabs.dispatch.sak.result.CaseDispatchResult;
import no.fintlabs.mapping.SakMappingService;
import no.fintlabs.model.instance.ArchiveInstance;
import no.fintlabs.model.instance.SakDto;
import no.fintlabs.web.archive.CaseSearchParametersService;
import no.fintlabs.web.archive.FintArchiveClient;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@Slf4j
public class CaseDispatchService {
    private final SakMappingService sakMappingService;
    private final CaseSearchParametersService caseSearchParametersService;
    private final FintArchiveClient fintArchiveClient;

    public CaseDispatchService(
            SakMappingService sakMappingService,
            CaseSearchParametersService caseSearchParametersService,
            FintArchiveClient fintArchiveClient
    ) {
        this.sakMappingService = sakMappingService;
        this.caseSearchParametersService = caseSearchParametersService;
        this.fintArchiveClient = fintArchiveClient;
    }

    public Mono<CaseDispatchResult> dispatch(SakDto sakDto) {
        SakResource sakResource = sakMappingService.toSakResource(sakDto);
        log.info("Posting case");
        return fintArchiveClient.postCase(sakResource)
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
                });
    }

    public Mono<List<SakResource>> findCasesBySearch(ArchiveInstance archiveInstance) {
        String caseFilter = caseSearchParametersService.createFilterQueryParamValue(
                archiveInstance.getNewCase(),
                archiveInstance.getCaseSearchParameters()
        );
        return fintArchiveClient.findCasesWithFilter(caseFilter);
    }

}
