package no.novari.flyt.archive.gateway.dispatch.file;

import lombok.AllArgsConstructor;
import no.fint.model.resource.Link;
import no.novari.flyt.archive.gateway.dispatch.file.result.FileDispatchResult;
import no.novari.flyt.archive.gateway.dispatch.file.result.FilesDispatchResult;
import no.novari.flyt.archive.gateway.dispatch.model.instance.DokumentobjektDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class FilesDispatchServiceTest {

    @Mock
    private FileDispatchService fileDispatchService;
    @InjectMocks
    private FilesDispatchService filesDispatchService;
    private Random random;

    @BeforeEach
    public void setup() {
        random = new Random(42);
    }

    @Test
    public void givenNoDokumentBeskrivelseDtosShouldReturnAcceptedResultWithNoDispatches() {
        StepVerifier.create(
                        filesDispatchService.dispatch(List.of())
                )
                .expectNext(FilesDispatchResult.accepted(Map.of()))
                .verifyComplete();
    }

    @Test
    public void givenAcceptedDispatchOfSingleDokumentobjektShouldReturnAcceptedResultWithSingleDispatch() {
        DokumentobjektMock dokumentobjektMock = mockDokumentobjektWithAcceptedDispatch();

        StepVerifier
                .create(filesDispatchService.dispatch(List.of(dokumentobjektMock.dokumentobjektDto)))
                .expectNext(FilesDispatchResult.accepted(Map.of(dokumentobjektMock.fileId, dokumentobjektMock.archiveLink)))
                .verifyComplete();

        verify(fileDispatchService, times(1)).dispatch(dokumentobjektMock.dokumentobjektDto);
        verifyNoMoreInteractions(fileDispatchService);
    }

    @Test
    public void givenAcceptedDispatchOfMultipleDokumentobjektShouldReturnAcceptedResultWithMultipleDispatch() {
        DokumentobjektMock dokumentobjektMock1 = mockDokumentobjektWithAcceptedDispatch();
        DokumentobjektMock dokumentobjektMock2 = mockDokumentobjektWithAcceptedDispatch();

        StepVerifier.create(
                        filesDispatchService.dispatch(List.of(
                                dokumentobjektMock1.dokumentobjektDto,
                                dokumentobjektMock2.dokumentobjektDto
                        ))
                )
                .expectNext(FilesDispatchResult.accepted(
                        Map.of(
                                dokumentobjektMock1.fileId, dokumentobjektMock1.archiveLink,
                                dokumentobjektMock2.fileId, dokumentobjektMock2.archiveLink
                        ))
                )
                .verifyComplete();

        verify(fileDispatchService, times(1)).dispatch(dokumentobjektMock1.dokumentobjektDto);
        verify(fileDispatchService, times(1)).dispatch(dokumentobjektMock2.dokumentobjektDto);
        verifyNoMoreInteractions(fileDispatchService);
    }

    @Test
    public void givenDeclinedDispatchOfSingleDokumentobjektShouldReturnDeclinedResult() {
        DokumentobjektMock dokumentobjektMock = mockDokumentobjektWithDeclinedDispatch("test error message");

        StepVerifier.create(filesDispatchService.dispatch(List.of(dokumentobjektMock.dokumentobjektDto)))
                .expectNext(FilesDispatchResult.declined("test error message"))
                .verifyComplete();

        verify(fileDispatchService, times(1)).dispatch(dokumentobjektMock.dokumentobjektDto);
        verifyNoMoreInteractions(fileDispatchService);
    }

    @Test
    public void givenDeclinedDispatchOfLastOfMultipleDokumentobjektShouldReturnDeclinedResult() {
        DokumentobjektMock dokumentobjektMock1 = mockDokumentobjektWithAcceptedDispatch();
        DokumentobjektMock dokumentobjektMock2 = mockDokumentobjektWithAcceptedDispatch();
        DokumentobjektMock dokumentobjektMock3 = mockDokumentobjektWithDeclinedDispatch("test error message");

        StepVerifier.create(filesDispatchService.dispatch(List.of(
                        dokumentobjektMock1.dokumentobjektDto,
                        dokumentobjektMock2.dokumentobjektDto,
                        dokumentobjektMock3.dokumentobjektDto
                )))
                .expectNext(FilesDispatchResult.declined("test error message"))
                .verifyComplete();

        verify(fileDispatchService, times(1)).dispatch(dokumentobjektMock1.dokumentobjektDto);
        verify(fileDispatchService, times(1)).dispatch(dokumentobjektMock2.dokumentobjektDto);
        verify(fileDispatchService, times(1)).dispatch(dokumentobjektMock3.dokumentobjektDto);
        verifyNoMoreInteractions(fileDispatchService);
    }

    @Test
    public void givenFailedDispatchOfSingleDokumentobjektShouldReturnFailedResult() {
        DokumentobjektMock dokumentobjektMock = mockDokumentobjektWithFailedDispatch();

        StepVerifier.create(filesDispatchService.dispatch(List.of(dokumentobjektMock.dokumentobjektDto)))
                .expectNext(FilesDispatchResult.failed())
                .verifyComplete();

        verify(fileDispatchService, times(1)).dispatch(dokumentobjektMock.dokumentobjektDto);
        verifyNoMoreInteractions(fileDispatchService);
    }

    @Test
    public void givenFailedDispatchOfLastOfMultipleDokumentobjektShouldReturnFailedResult() {
        DokumentobjektMock dokumentobjektMock1 = mockDokumentobjektWithAcceptedDispatch();
        DokumentobjektMock dokumentobjektMock2 = mockDokumentobjektWithAcceptedDispatch();
        DokumentobjektMock dokumentobjektMock3 = mockDokumentobjektWithFailedDispatch();

        StepVerifier.create(filesDispatchService.dispatch(List.of(
                        dokumentobjektMock1.dokumentobjektDto,
                        dokumentobjektMock2.dokumentobjektDto,
                        dokumentobjektMock3.dokumentobjektDto
                )))
                .expectNext(FilesDispatchResult.failed())
                .verifyComplete();

        verify(fileDispatchService, times(1)).dispatch(dokumentobjektMock1.dokumentobjektDto);
        verify(fileDispatchService, times(1)).dispatch(dokumentobjektMock2.dokumentobjektDto);
        verify(fileDispatchService, times(1)).dispatch(dokumentobjektMock3.dokumentobjektDto);
        verifyNoMoreInteractions(fileDispatchService);
    }

    private DokumentobjektMock mockDokumentobjektWithAcceptedDispatch() {
        DokumentobjektMock dokumentobjektMock = mockDokumentobjekt();
        doReturn(Mono.just(FileDispatchResult.accepted(dokumentobjektMock.fileId, dokumentobjektMock.archiveLink)))
                .when(fileDispatchService).dispatch(dokumentobjektMock.dokumentobjektDto);
        return dokumentobjektMock;
    }

    private DokumentobjektMock mockDokumentobjektWithDeclinedDispatch(String errorMessage) {
        DokumentobjektMock dokumentobjektMock = mockDokumentobjekt();
        doReturn(Mono.just(FileDispatchResult.declined(dokumentobjektMock.fileId, errorMessage)))
                .when(fileDispatchService).dispatch(dokumentobjektMock.dokumentobjektDto);
        return dokumentobjektMock;
    }

    private DokumentobjektMock mockDokumentobjektWithFailedDispatch() {
        DokumentobjektMock dokumentobjektMock = mockDokumentobjekt();
        doReturn(Mono.just(FileDispatchResult.failed(dokumentobjektMock.fileId)))
                .when(fileDispatchService).dispatch(dokumentobjektMock.dokumentobjektDto);
        return dokumentobjektMock;
    }

    private DokumentobjektMock mockDokumentobjekt() {
        UUID fileId = getUuid();
        return new DokumentobjektMock(
                fileId,
                mock(Link.class),
                DokumentobjektDto.builder().fileId(fileId).build()
        );
    }

    @AllArgsConstructor
    private static class DokumentobjektMock {
        private final UUID fileId;
        private final Link archiveLink;
        private final DokumentobjektDto dokumentobjektDto;
    }

    private UUID getUuid() {
        byte[] bytes = new byte[7];
        random.nextBytes(bytes);
        return UUID.nameUUIDFromBytes(bytes);
    }


}