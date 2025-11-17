package no.novari.flyt.archive.gateway.dispatch.mapping;

import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.DokumentobjektResource;
import no.novari.flyt.archive.gateway.dispatch.model.instance.DokumentobjektDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DokumentObjektMappingServiceTest {

    @InjectMocks
    private DokumentObjektMappingService mappingService;

    @BeforeEach
    void setUp() {
        mappingService = new DokumentObjektMappingService();
    }

    @Test
    void testToDokumentobjektResource() {
        UUID fileId = UUID.randomUUID();
        DokumentobjektDto dto = DokumentobjektDto
                .builder()
                .variantformat("testVariantFormat")
                .filformat("testFilFormat")
                .format("testFormat")
                .fileId(fileId)
                .build();
        Link fileLink = new Link("mockedLinkValue");

        Map<UUID, Link> fileIdMap = new HashMap<>();
        fileIdMap.put(fileId, fileLink);

        DokumentobjektResource resource = mappingService.toDokumentobjektResource(dto, fileIdMap);
        assertThat(resource).isNotNull();
        assertThat(resource.getVariantFormat()).containsOnly(Link.with("testVariantFormat"));
        assertThat(resource.getFilformat()).containsOnly(Link.with("testFilFormat"));
        assertThat(resource.getFormat()).isEqualTo("testFormat");
        assertThat(resource.getReferanseDokumentfil()).contains(fileLink);
    }

    @Test
    void testToDokumentobjektResourceList() {
        DokumentobjektDto dto1 = mock(DokumentobjektDto.class);
        DokumentobjektDto dto2 = mock(DokumentobjektDto.class);
        UUID fileId1 = UUID.randomUUID();
        UUID fileId2 = UUID.randomUUID();
        Link fileLink1 = mock(Link.class);
        Link fileLink2 = mock(Link.class);

        when(dto1.getFileId()).thenReturn(Optional.of(fileId1));
        when(dto2.getFileId()).thenReturn(Optional.of(fileId2));

        Map<UUID, Link> fileIdMap = new HashMap<>();
        fileIdMap.put(fileId1, fileLink1);
        fileIdMap.put(fileId2, fileLink2);

        List<DokumentobjektResource> resources = mappingService.toDokumentobjektResource(Arrays.asList(dto1, dto2), fileIdMap);

        assertThat(resources).isNotNull();
        assertThat(resources.size()).isEqualTo(2);
    }
}
