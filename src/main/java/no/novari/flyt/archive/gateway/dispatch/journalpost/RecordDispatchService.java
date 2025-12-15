package no.novari.flyt.archive.gateway.dispatch.journalpost;

import io.netty.handler.timeout.ReadTimeoutException;
import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.JournalpostResource;
import no.novari.flyt.archive.gateway.dispatch.file.FilesDispatchService;
import no.novari.flyt.archive.gateway.dispatch.journalpost.result.RecordDispatchResult;
import no.novari.flyt.archive.gateway.dispatch.mapping.JournalpostMappingService;
import no.novari.flyt.archive.gateway.dispatch.model.instance.DokumentbeskrivelseDto;
import no.novari.flyt.archive.gateway.dispatch.model.instance.DokumentobjektDto;
import no.novari.flyt.archive.gateway.dispatch.model.instance.JournalpostDto;
import no.novari.flyt.archive.gateway.dispatch.web.CreatedLocationPollTimeoutException;
import no.novari.flyt.archive.gateway.dispatch.web.FintArchiveDispatchClient;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class RecordDispatchService {

    private final JournalpostMappingService journalpostMappingService;
    private final FilesDispatchService filesDispatchService;

    private final FintArchiveDispatchClient fintArchiveDispatchClient;

    public RecordDispatchService(
            JournalpostMappingService journalpostMappingService,
            FilesDispatchService filesDispatchService,
            FintArchiveDispatchClient fintArchiveDispatchClient
    ) {
        this.journalpostMappingService = journalpostMappingService;
        this.filesDispatchService = filesDispatchService;
        this.fintArchiveDispatchClient = fintArchiveDispatchClient;
    }

    public Mono<RecordDispatchResult> dispatch(String caseId, JournalpostDto journalpostDto) {
        log.info("Dispatching record");
        List<DokumentobjektDto> dokumentobjektDtos = journalpostDto.getDokumentbeskrivelse()
                .map(this::getDokumentObjektDtos)
                .orElse(List.of());
        if (dokumentobjektDtos.isEmpty()) {
            return dispatch(caseId, journalpostDto, Map.of());
        }
        return filesDispatchService.dispatch(dokumentobjektDtos)
                .flatMap(filesDispatchResult -> switch (filesDispatchResult.getStatus()) {
                    case ACCEPTED ->
                            dispatch(caseId, journalpostDto, filesDispatchResult.getArchiveFileLinkPerFileId());
                    case DECLINED -> Mono.just(
                            RecordDispatchResult.declined(
                                    "Dokumentobjekt declined by destination with message='" +
                                    filesDispatchResult.getErrorMessage() + "'"
                            )
                    );
                    case FAILED -> Mono.just(
                            RecordDispatchResult.failed("Dokumentobjekt dispatch failed")
                    );
                }).doOnNext(result -> log.info("Dispatch result={}", result.toString()));
    }

    private List<DokumentobjektDto> getDokumentObjektDtos(Collection<DokumentbeskrivelseDto> dokumentbeskrivelseDtos) {
        return dokumentbeskrivelseDtos
                .stream()
                .map(DokumentbeskrivelseDto::getDokumentobjekt)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .flatMap(Collection::stream)
                .toList();
    }

    private Mono<RecordDispatchResult> dispatch(
            String caseId,
            JournalpostDto journalpostDto,
            Map<UUID, Link> archiveFileLinkPerFileId
    ) {
        JournalpostResource journalpostResource = journalpostMappingService.toJournalpostResource(
                journalpostDto,
                archiveFileLinkPerFileId
        );
        return fintArchiveDispatchClient.postRecord(caseId, journalpostResource)
                .map(JournalpostResource::getJournalPostnummer)
                .map(RecordDispatchResult::accepted)
                .onErrorResume(WebClientResponseException.class,
                        e -> Mono.just(RecordDispatchResult.declined(e.getResponseBodyAsString()))
                ).onErrorResume(
                        e -> e instanceof ReadTimeoutException || e instanceof CreatedLocationPollTimeoutException,
                        e -> {
                            log.error("Record dispatch timed out");
                            return Mono.just(RecordDispatchResult.timedOut());
                        }
                ).onErrorResume(e -> {
                    log.error("Failed to post record", e);
                    return Mono.just(RecordDispatchResult.failed(null));
                });
    }

}
