package no.novari.flyt.archive.gateway.dispatch.mapping;

import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.DokumentbeskrivelseResource;
import no.novari.flyt.archive.gateway.dispatch.model.instance.DokumentbeskrivelseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class DokumentbeskrivelseMappingServiceTest {

    @InjectMocks
    private DokumentbeskrivelseMappingService dokumentbeskrivelseMappingService;

    @SuppressWarnings("unused")
    @Mock
    private SkjermingMappingService skjermingMappingService;

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

        assertThat(resource.getTittel()).isEqualTo("testTittel");
        assertThat(resource.getDokumentType()).isEqualTo(Collections.singletonList(Link.with("testDokumenttype")));
        assertThat(resource.getDokumentstatus()).isEqualTo(Collections.singletonList(Link.with("testDokumentstatus")));
        assertThat(resource.getTilknyttetRegistreringSom()).isEqualTo(Collections.singletonList(Link.with("testTilknyttetRegistreringSom")));
    }
}
