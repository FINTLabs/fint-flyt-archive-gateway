package no.fintlabs.mapping;

import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.DokumentobjektResource;
import no.fintlabs.model.instance.DokumentobjektDto;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DokumentObjektMappingService {

    public List<DokumentobjektResource> toDokumentobjektResource(
            Collection<DokumentobjektDto> dokumentobjektDto,
            Map<UUID, Link> fileArchiveLinkPerFileId
    ) {
        return dokumentobjektDto
                .stream()
                .map(doDto -> toDokumentobjektResource(doDto, fileArchiveLinkPerFileId))
                .toList();
    }

    public DokumentobjektResource toDokumentobjektResource(
            DokumentobjektDto dokumentobjektDto,
            Map<UUID, Link> fileArchiveLinkPerFileId
    ) {
        DokumentobjektResource dokumentobjektResource = new DokumentobjektResource();
        dokumentobjektResource.addVariantFormat(dokumentobjektDto.getVariantformat());
        dokumentobjektResource.addFilformat(dokumentobjektDto.getFilformat());
        dokumentobjektResource.addReferanseDokumentfil(
                fileArchiveLinkPerFileId.get(dokumentobjektDto.getFileReference().getFileId())
        );
        return dokumentobjektResource;
    }

}
