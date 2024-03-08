package no.fintlabs.flyt.gateway.application.archive.sak;

import no.fint.model.resource.arkiv.noark.SakResource;
import no.fintlabs.flyt.gateway.application.archive.resource.sak.CaseController;
import no.fintlabs.flyt.gateway.application.archive.resource.sak.CaseRequestService;
import no.fintlabs.flyt.gateway.application.archive.resource.sak.CaseTitle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CaseControllerTest {

    @Mock
    CaseRequestService caseRequestService;

    @Mock
    SakResource sakResource;

    CaseController caseController;

    @BeforeEach
    public void setUp() {
        caseController = new CaseController(caseRequestService);
    }

    @Test
    public void getCaseTitle_givenFoundCase_shouldReturn200WithCaseTitle() {
        when(sakResource.getTittel()).thenReturn("Test tittel");
        when(caseRequestService.getByMappeId("2023/102")).thenReturn(Optional.of(sakResource));


        ResponseEntity<CaseTitle> response = caseController.getCaseTitle("2023", "102");

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertEquals(response.getBody().getValue(), "Test tittel");
        verify(caseRequestService, times(1)).getByMappeId("2023/102");
        verifyNoMoreInteractions(caseRequestService);
    }

    @Test
    public void getCaseTitle_givenNoFoundCase_shouldReturn404() {
        when(caseRequestService.getByMappeId("2023/101")).thenReturn(Optional.empty());

        ResponseStatusException responseStatusException = assertThrows(
                ResponseStatusException.class,
                () -> caseController.getCaseTitle("2023", "101")
        );

        assertEquals(HttpStatus.NOT_FOUND, responseStatusException.getStatus());
        assertEquals("Case with mappeId=2023/101 could not be found", responseStatusException.getReason());
        verify(caseRequestService, times(1)).getByMappeId("2023/101");
        verifyNoMoreInteractions(caseRequestService);
    }

}
