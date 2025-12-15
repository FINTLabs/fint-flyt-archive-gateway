package no.novari.flyt.archive.gateway.dispatch.file;

import io.netty.handler.timeout.ReadTimeoutException;
import lombok.AllArgsConstructor;
import no.fint.model.resource.Link;
import no.novari.flyt.archive.gateway.dispatch.file.result.FileDispatchResult;
import no.novari.flyt.archive.gateway.dispatch.model.File;
import no.novari.flyt.archive.gateway.dispatch.model.instance.DokumentobjektDto;
import no.novari.flyt.archive.gateway.dispatch.web.CreatedLocationPollTimeoutException;
import no.novari.flyt.archive.gateway.dispatch.web.FintArchiveDispatchClient;
import no.novari.flyt.archive.gateway.dispatch.web.flytfile.FlytFileClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Random;
import java.util.UUID;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;


@ExtendWith(MockitoExtension.class)
class FileDispatchServiceTest {

    @Mock
    private FintArchiveDispatchClient fintArchiveDispatchClient;
    @Mock
    private FlytFileClient flytFileClient;
    @InjectMocks
    private FileDispatchService fileDispatchService;
    private Random random;

    @BeforeEach
    public void setup() {
        random = new Random(42);
    }

    @Test
    public void givenSuccessFromGetFileAndSuccessFromPostFileShouldReturnAcceptedResult() {
        FileMock fileMock = mockFile();

        doReturn(Mono.just(fileMock.file)).when(flytFileClient).getFile(fileMock.fileId);
        doReturn(Mono.just(fileMock.archiveLink)).when(fintArchiveDispatchClient).postFile(fileMock.file);

        StepVerifier
                .create(fileDispatchService.dispatch(fileMock.dokumentobjektDto))
                .expectNext(FileDispatchResult.accepted(fileMock.fileId, fileMock.archiveLink))
                .verifyComplete();

        verify(flytFileClient, times(1)).getFile(fileMock.fileId);
        verifyNoMoreInteractions(flytFileClient);

        verify(fintArchiveDispatchClient, times(1)).postFile(fileMock.file);
        verifyNoMoreInteractions(fintArchiveDispatchClient);
    }

    @Test
    public void givenErrorFromGetFileShouldReturnFailedCouldNotBeRetrievedResult() {
        UUID fileId = getUuid();

        doReturn(Mono.error(new RuntimeException())).when(flytFileClient).getFile(fileId);

        DokumentobjektDto dokumentobjektDto = DokumentobjektDto
                .builder()
                .fileId(fileId)
                .build();

        StepVerifier
                .create(fileDispatchService.dispatch(dokumentobjektDto))
                .expectNext(FileDispatchResult.couldNotBeRetrieved(fileId))
                .verifyComplete();

        verify(flytFileClient, times(1)).getFile(fileId);
        verifyNoMoreInteractions(flytFileClient);
    }

    @Test
    public void givenSuccessFromGetFileAndWebClientResponseExceptionFromPostFileShouldReturnDeclinedResult() {
        FileMock fileMock = mockFile();

        doReturn(Mono.just(fileMock.file)).when(flytFileClient).getFile(fileMock.fileId);

        WebClientResponseException webClientResponseException = mock(WebClientResponseException.class);
        doReturn("test response body").when(webClientResponseException).getResponseBodyAsString();
        doReturn(Mono.error(webClientResponseException)).when(fintArchiveDispatchClient).postFile(fileMock.file);

        StepVerifier
                .create(fileDispatchService.dispatch(fileMock.dokumentobjektDto))
                .expectNext(FileDispatchResult.declined(fileMock.fileId, "test response body"))
                .verifyComplete();

        verify(flytFileClient, times(1)).getFile(fileMock.fileId);
        verifyNoMoreInteractions(flytFileClient);

        verify(fintArchiveDispatchClient, times(1)).postFile(fileMock.file);
        verifyNoMoreInteractions(fintArchiveDispatchClient);
    }

    @Test
    public void givenSuccessFromGetFileAndReadTimeoutExceptionFromDispatchClientShouldReturnFailedTimedOutResult() {
        FileMock fileMock = mockFile();
        doReturn(Mono.just(fileMock.file)).when(flytFileClient).getFile(fileMock.fileId);
        doReturn(Mono.error(new ReadTimeoutException())).when(fintArchiveDispatchClient).postFile(fileMock.file);

        StepVerifier
                .create(fileDispatchService.dispatch(fileMock.dokumentobjektDto))
                .expectNext(FileDispatchResult.timedOut(fileMock.fileId))
                .verifyComplete();

        verify(flytFileClient, times(1)).getFile(fileMock.fileId);
        verifyNoMoreInteractions(flytFileClient);

        verify(fintArchiveDispatchClient, times(1)).postFile(fileMock.file);
        verifyNoMoreInteractions(fintArchiveDispatchClient);
    }

    @Test
    public void givenSuccessFromGetFileAndCreatedLocationPollTimeoutExceptionFromDispatchClientShouldReturnFailedTimedOutResult() {
        FileMock fileMock = mockFile();
        doReturn(Mono.just(fileMock.file)).when(flytFileClient).getFile(fileMock.fileId);
        doReturn(Mono.error(new CreatedLocationPollTimeoutException())).when(fintArchiveDispatchClient).postFile(fileMock.file);

        StepVerifier
                .create(fileDispatchService.dispatch(fileMock.dokumentobjektDto))
                .expectNext(FileDispatchResult.timedOut(fileMock.fileId))
                .verifyComplete();

        verify(flytFileClient, times(1)).getFile(fileMock.fileId);
        verifyNoMoreInteractions(flytFileClient);

        verify(fintArchiveDispatchClient, times(1)).postFile(fileMock.file);
        verifyNoMoreInteractions(fintArchiveDispatchClient);
    }

    @Test
    public void givenSuccessFromGetFileAndErrorOtherThanWebClientResponseExceptionAndTimeoutExceptionFromPostFileShouldReturnFailedResult() {
        FileMock fileMock = mockFile();

        doReturn(Mono.just(fileMock.file)).when(flytFileClient).getFile(fileMock.fileId);
        doReturn(Mono.error(new RuntimeException())).when(fintArchiveDispatchClient).postFile(fileMock.file);

        StepVerifier
                .create(fileDispatchService.dispatch(fileMock.dokumentobjektDto))
                .expectNext(FileDispatchResult.failed(fileMock.fileId))
                .verifyComplete();

        verify(flytFileClient, times(1)).getFile(fileMock.fileId);
        verifyNoMoreInteractions(flytFileClient);

        verify(fintArchiveDispatchClient, times(1)).postFile(fileMock.file);
        verifyNoMoreInteractions(fintArchiveDispatchClient);
    }

    private FileMock mockFile() {
        UUID fileId = getUuid();
        return new FileMock(
                fileId,
                mock(File.class),
                mock(Link.class),
                DokumentobjektDto.builder().fileId(fileId).build()
        );
    }

    @AllArgsConstructor
    private static class FileMock {
        private final UUID fileId;
        private final File file;
        private final Link archiveLink;
        private final DokumentobjektDto dokumentobjektDto;
    }

    private UUID getUuid() {
        byte[] bytes = new byte[7];
        random.nextBytes(bytes);
        return UUID.nameUUIDFromBytes(bytes);
    }

}