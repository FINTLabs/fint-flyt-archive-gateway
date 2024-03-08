package no.fintlabs.flyt.gateway.application.archive.dispatch.mapping;

import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.DokumentbeskrivelseResource;
import no.fintlabs.flyt.gateway.application.archive.dispatch.model.instance.DokumentbeskrivelseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DokumentbeskrivelseMappingServiceTest {

    @InjectMocks
    private DokumentbeskrivelseMappingService dokumentbeskrivelseMappingService;

    @SuppressWarnings("unused")
    @Mock
    private SkjermingMappingService skjermingMappingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void toDokumentbeskrivelseResource() {
        DokumentbeskrivelseDto dto = DokumentbeskrivelseDto.builder()
                .tittel("testTittel")
                .dokumentstatus("testDokumentstatus")
                .dokumentType("testDokumenttype")
                .tilknyttetRegistreringSom("testTilknyttetRegistreringSom")
                .build();

        HashMap<UUID, Link> linkMap = new HashMap<>();

        DokumentbeskrivelseResource resource = dokumentbeskrivelseMappingService.toDokumentbeskrivelseResource(dto, linkMap);

        assertEquals("testTittel", resource.getTittel());
        assertEquals(Collections.singletonList(Link.with("testDokumenttype")), resource.getDokumentType());
        assertEquals(Collections.singletonList(Link.with("testDokumentstatus")), resource.getDokumentstatus());
        assertEquals(Collections.singletonList(Link.with("testTilknyttetRegistreringSom")), resource.getTilknyttetRegistreringSom());
    }
}
