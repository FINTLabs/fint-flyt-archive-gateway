package no.novari.flyt.archive.gateway.dispatch.file;

import lombok.extern.slf4j.Slf4j;
import no.novari.flyt.archive.gateway.dispatch.DispatchStatus;
import no.novari.flyt.archive.gateway.dispatch.file.result.FileDispatchResult;
import no.novari.flyt.archive.gateway.dispatch.file.result.FilesDispatchResult;
import no.novari.flyt.archive.gateway.dispatch.model.instance.DokumentobjektDto;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

@Service
@Slf4j
public class FilesDispatchService {

    private final FileDispatchService fileDispatchService;


    public FilesDispatchService(
            FileDispatchService fileDispatchService
    ) {
        this.fileDispatchService = fileDispatchService;
    }

    public Mono<FilesDispatchResult> dispatch(Collection<DokumentobjektDto> dokumentobjektDtos) {
        if (dokumentobjektDtos.isEmpty()) {
            log.info("No files to dispatch");
            return Mono.just(FilesDispatchResult.accepted(Map.of()));
        }
        log.info("Dispatching files");
        return Flux.fromStream(dokumentobjektDtos.stream())
                .concatMap(fileDispatchService::dispatch)
                .takeUntil(fileDispatchResult -> fileDispatchResult.getStatus() != DispatchStatus.ACCEPTED)
                .collectList()
                .map(fileDispatchResults -> {

                    FileDispatchResult lastResult = fileDispatchResults.getLast();
                    DispatchStatus lastStatus = lastResult.getStatus();

                    return switch (lastStatus) {
                        case ACCEPTED -> FilesDispatchResult.accepted(
                                fileDispatchResults
                                        .stream()
                                        .collect(toMap(
                                                FileDispatchResult::getFileId,
                                                FileDispatchResult::getArchiveFileLink
                                        ))
                        );
                        case DECLINED -> FilesDispatchResult.declined(
                                lastResult.getErrorMessage()
                        );
                        case FAILED -> FilesDispatchResult.failed();

                    };

                }).doOnNext(result -> log.info("Dispatch result={}", result.toString()));
    }

}
