package no.fintlabs.flyt.gateway.application.archive.dispatch.mapping;

import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.DokumentobjektResource;
import no.fintlabs.flyt.gateway.application.archive.dispatch.model.instance.DokumentobjektDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class DokumentObjektMappingServiceTest {

    @InjectMocks
    private DokumentObjektMappingService mappingService;

    @BeforeEach
    void setUp() {
        mappingService = new DokumentObjektMappingService();
    }

    // TODO: 11/08/2023 need more assertions 
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
        System.out.println(resource.getVariantFormat());
        assertNotNull(resource);
//        assertTrue(resource.getVariantFormat().contains(fileLink));
//        assertTrue(resource.getFilformat().contains(fileLink));
        assertEquals("testFormat", resource.getFormat());
        assertTrue(resource.getReferanseDokumentfil().contains(fileLink));
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

        assertNotNull(resources);
        assertEquals(2, resources.size());
    }
}
