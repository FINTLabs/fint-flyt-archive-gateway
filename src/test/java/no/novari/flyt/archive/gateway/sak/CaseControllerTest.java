package no.novari.flyt.archive.gateway.sak;

import no.fint.model.resource.arkiv.noark.SakResource;
import no.novari.flyt.archive.gateway.resource.sak.CaseController;
import no.novari.flyt.archive.gateway.resource.sak.CaseRequestService;
import no.novari.flyt.archive.gateway.resource.sak.CaseTitle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

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

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getValue()).isEqualTo("Test tittel");

        verify(caseRequestService, times(1)).getByMappeId("2023/102");
        verifyNoMoreInteractions(caseRequestService);
    }

    @Test
    public void getCaseTitle_givenNoFoundCase_shouldReturn404() {
        when(caseRequestService.getByMappeId("2023/101")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> caseController.getCaseTitle("2023", "101"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Case with mappeId=2023/101 could not be found");

        verify(caseRequestService, times(1)).getByMappeId("2023/101");
        verifyNoMoreInteractions(caseRequestService);
    }

}
