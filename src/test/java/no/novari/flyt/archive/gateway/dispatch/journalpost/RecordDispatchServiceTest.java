package no.novari.flyt.archive.gateway.dispatch.journalpost;

import io.netty.handler.timeout.ReadTimeoutException;
import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.JournalpostResource;
import no.novari.flyt.archive.gateway.dispatch.file.FilesDispatchService;
import no.novari.flyt.archive.gateway.dispatch.file.result.FilesDispatchResult;
import no.novari.flyt.archive.gateway.dispatch.journalpost.result.RecordDispatchResult;
import no.novari.flyt.archive.gateway.dispatch.mapping.JournalpostMappingService;
import no.novari.flyt.archive.gateway.dispatch.model.instance.DokumentbeskrivelseDto;
import no.novari.flyt.archive.gateway.dispatch.model.instance.DokumentobjektDto;
import no.novari.flyt.archive.gateway.dispatch.model.instance.JournalpostDto;
import no.novari.flyt.archive.gateway.dispatch.web.CreatedLocationPollTimeoutException;
import no.novari.flyt.archive.gateway.dispatch.web.FintArchiveDispatchClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class RecordDispatchServiceTest {

    @Mock
    JournalpostMappingService journalpostMappingService;

    @Mock
    FilesDispatchService filesDispatchService;

    @Mock
    FintArchiveDispatchClient fintArchiveDispatchClient;

    @InjectMocks
    RecordDispatchService recordDispatchService;

    private Random random;

    @BeforeEach
    public void setup() {
        random = new Random(42);
    }

    @Test
    public void givenJournalpostDtoWithNoDokumentbeskrivelseDtosShouldCallMappingServiceWithEmptyDokumentobjektList() {
        JournalpostDto journalpostDto = mock(JournalpostDto.class);
        doReturn(Optional.empty()).when(journalpostDto).getDokumentbeskrivelse();

        doReturn(Mono.empty()).when(fintArchiveDispatchClient).postRecord(any(), any());

        StepVerifier
                .create(recordDispatchService.dispatch("testCaseId", journalpostDto))
                .verifyComplete();

        verify(journalpostMappingService, times(1))
                .toJournalpostResource(journalpostDto, Map.of());
        verifyNoMoreInteractions(journalpostMappingService);
    }

    @Test
    public void givenJournalpostDtoWithDokumentbeskrivelseDtosShouldDispatchDokumentobjektDtos() {
        JournalpostDto journalpostDto = mock(JournalpostDto.class);
        DokumentobjektDto dokumentobjektDto1 = mock(DokumentobjektDto.class);
        DokumentobjektDto dokumentobjektDto2 = mock(DokumentobjektDto.class);
        DokumentobjektDto dokumentobjektDto3 = mock(DokumentobjektDto.class);
        doReturn(Optional.of(List.of(
                DokumentbeskrivelseDto
                        .builder()
                        .dokumentobjekt(List.of(dokumentobjektDto1, dokumentobjektDto2))
                        .build(),
                DokumentbeskrivelseDto
                        .builder()
                        .dokumentobjekt(List.of(dokumentobjektDto3))
                        .build()
        ))).when(journalpostDto).getDokumentbeskrivelse();

        doReturn(Mono.empty()).when(filesDispatchService).dispatch(any());

        StepVerifier
                .create(recordDispatchService.dispatch("testCaseId", journalpostDto))
                .verifyComplete();

        verify(filesDispatchService, times(1))
                .dispatch(List.of(dokumentobjektDto1, dokumentobjektDto2, dokumentobjektDto3));
        verifyNoMoreInteractions(filesDispatchService);
    }

    @Test
    public void givenAcceptedFilesDispatchShouldCallMappingServiceWithArchiveFileLinkPerFileId() {
        JournalpostDto journalpostDto = mock(JournalpostDto.class);
        DokumentobjektDto dokumentobjektDto1 = mock(DokumentobjektDto.class);
        DokumentobjektDto dokumentobjektDto2 = mock(DokumentobjektDto.class);
        DokumentobjektDto dokumentobjektDto3 = mock(DokumentobjektDto.class);
        doReturn(Optional.of(List.of(
                DokumentbeskrivelseDto
                        .builder()
                        .dokumentobjekt(List.of(dokumentobjektDto1, dokumentobjektDto2))
                        .build(),
                DokumentbeskrivelseDto
                        .builder()
                        .dokumentobjekt(List.of(dokumentobjektDto3))
                        .build()
        ))).when(journalpostDto).getDokumentbeskrivelse();

        UUID uuid1 = getUuid();
        UUID uuid2 = getUuid();
        UUID uuid3 = getUuid();

        Link link1 = mock(Link.class);
        Link link2 = mock(Link.class);
        Link link3 = mock(Link.class);

        doReturn(
                Mono.just(FilesDispatchResult.accepted(Map.of(
                        uuid1, link1,
                        uuid2, link2,
                        uuid3, link3
                )))
        ).when(filesDispatchService).dispatch(any());

        doReturn(Mono.empty()).when(fintArchiveDispatchClient).postRecord(any(), any());

        StepVerifier
                .create(recordDispatchService.dispatch("testCaseId", journalpostDto))
                .verifyComplete();

        verify(journalpostMappingService, times(1))
                .toJournalpostResource(journalpostDto, Map.of(
                        uuid1, link1,
                        uuid2, link2,
                        uuid3, link3
                ));
        verifyNoMoreInteractions(journalpostMappingService);
    }

    @Test
    public void givenCaseIdAndJournalpostResourceFromMapperShouldCallPostRecordWithCaseIdAndJournalpostResource() {
        String caseId = "testCaseId";

        JournalpostDto journalpostDto = mock(JournalpostDto.class);
        doReturn(Optional.empty()).when(journalpostDto).getDokumentbeskrivelse();

        JournalpostResource journalpostResource = mock(JournalpostResource.class);
        doReturn(journalpostResource).when(journalpostMappingService).toJournalpostResource(any(), any());

        doReturn(Mono.empty()).when(fintArchiveDispatchClient).postRecord(any(), any());

        StepVerifier
                .create(recordDispatchService.dispatch("testCaseId", journalpostDto))
                .verifyComplete();

        verify(fintArchiveDispatchClient, times(1))
                .postRecord(caseId, journalpostResource);
        verifyNoMoreInteractions(fintArchiveDispatchClient);
    }

    @Test
    public void givenSuccessfulArchiveClientPostRecordShouldReturnAcceptedResultWithJournalpostnummer() {
        JournalpostDto journalpostDto = mock(JournalpostDto.class);
        doReturn(Optional.empty()).when(journalpostDto).getDokumentbeskrivelse();

        JournalpostResource journalpostResource = mock(JournalpostResource.class);
        doReturn(journalpostResource).when(journalpostMappingService).toJournalpostResource(any(), any());

        JournalpostResource journalpostResourcePostResult = mock(JournalpostResource.class);
        doReturn(123L).when(journalpostResourcePostResult).getJournalPostnummer();
        doReturn(Mono.just(journalpostResourcePostResult)).when(fintArchiveDispatchClient).postRecord(any(), any());

        StepVerifier
                .create(recordDispatchService.dispatch("testCaseId", journalpostDto))
                .expectNext(RecordDispatchResult.accepted(123L))
                .verifyComplete();
    }

    @Test
    public void givenWebclientResponseExceptionFromArchiveClientPostRecordShouldReturnDeclinedResultWithErrorMessage() {
        JournalpostDto journalpostDto = mock(JournalpostDto.class);

        JournalpostResource journalpostResource = mock(JournalpostResource.class);
        doReturn(journalpostResource).when(journalpostMappingService).toJournalpostResource(any(), any());

        WebClientResponseException webClientResponseException = mock(WebClientResponseException.class);
        doReturn("test response body").when(webClientResponseException).getResponseBodyAsString();
        doReturn(Mono.error(webClientResponseException)).when(fintArchiveDispatchClient).postRecord(any(), any());

        StepVerifier
                .create(recordDispatchService.dispatch("testCaseId", journalpostDto))
                .expectNext(RecordDispatchResult.declined(
                        "test response body"
                ))
                .verifyComplete();
    }

    @Test
    public void givenReadTimeoutExceptionFromArchiveClientPostRecordShouldReturnFailedTimedOutResult() {
        JournalpostDto journalpostDto = mock(JournalpostDto.class);

        JournalpostResource journalpostResource = mock(JournalpostResource.class);
        doReturn(journalpostResource).when(journalpostMappingService).toJournalpostResource(any(), any());

        doReturn(Mono.error(new ReadTimeoutException())).when(fintArchiveDispatchClient).postRecord(any(), any());

        StepVerifier
                .create(recordDispatchService.dispatch("testCaseId", journalpostDto))
                .expectNext(RecordDispatchResult.timedOut())
                .verifyComplete();
    }

    @Test
    public void givenCreatedLocationPollExceptionFromArchiveClientPostRecordShouldReturnFailedTimedOutResult() {
        JournalpostDto journalpostDto = mock(JournalpostDto.class);

        JournalpostResource journalpostResource = mock(JournalpostResource.class);
        doReturn(journalpostResource).when(journalpostMappingService).toJournalpostResource(any(), any());

        doReturn(Mono.error(new CreatedLocationPollTimeoutException())).when(fintArchiveDispatchClient).postRecord(any(), any());

        StepVerifier
                .create(recordDispatchService.dispatch("testCaseId", journalpostDto))
                .expectNext(RecordDispatchResult.timedOut())
                .verifyComplete();
    }

    @Test
    public void givenExceptionOtherThanWebclientResponseExceptionAndTimeoutExceptionFromArchiveClientPostRecordShouldReturnFailedResultWithoutErrorMessage() {
        JournalpostDto journalpostDto = mock(JournalpostDto.class);

        JournalpostResource journalpostResource = mock(JournalpostResource.class);
        doReturn(journalpostResource).when(journalpostMappingService).toJournalpostResource(any(), any());

        doReturn(Mono.error(new RuntimeException())).when(fintArchiveDispatchClient).postRecord(any(), any());

        StepVerifier
                .create(recordDispatchService.dispatch("testCaseId", journalpostDto))
                .expectNext(RecordDispatchResult.failed(
                        null
                ))
                .verifyComplete();
    }

    @Test
    public void givenDeclinedFilesDispatchWithErrorMessageShouldReturnDeclinedResultWithErrorMessage() {
        JournalpostDto journalpostDto = mock(JournalpostDto.class);
        DokumentobjektDto dokumentobjektDto = mock(DokumentobjektDto.class);
        doReturn(Optional.of(List.of(
                DokumentbeskrivelseDto
                        .builder()
                        .dokumentobjekt(List.of(dokumentobjektDto))
                        .build()
        ))).when(journalpostDto).getDokumentbeskrivelse();

        doReturn(
                Mono.just(FilesDispatchResult.declined(
                        "test error message"
                ))
        ).when(filesDispatchService).dispatch(any());

        StepVerifier
                .create(recordDispatchService.dispatch("testCaseId", journalpostDto))
                .expectNext(RecordDispatchResult.declined(
                        "Dokumentobjekt declined by destination with message='test error message'"
                ))
                .verifyComplete();
    }

    @Test
    public void givenFailedFilesDispatchShouldReturnDeclinedResultWithErrorMessage() {
        JournalpostDto journalpostDto = mock(JournalpostDto.class);
        DokumentobjektDto dokumentobjektDto = mock(DokumentobjektDto.class);
        doReturn(Optional.of(List.of(
                DokumentbeskrivelseDto
                        .builder()
                        .dokumentobjekt(List.of(dokumentobjektDto))
                        .build()
        ))).when(journalpostDto).getDokumentbeskrivelse();

        doReturn(
                Mono.just(FilesDispatchResult.failed())
        ).when(filesDispatchService).dispatch(any());

        StepVerifier
                .create(recordDispatchService.dispatch("testCaseId", journalpostDto))
                .expectNext(RecordDispatchResult.failed(
                        "Dokumentobjekt dispatch failed"
                ))
                .verifyComplete();
    }

    private UUID getUuid() {
        byte[] bytes = new byte[7];
        random.nextBytes(bytes);
        return UUID.nameUUIDFromBytes(bytes);
    }

}
