package no.novari.flyt.archive.gateway.dispatch.mapping;

import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.DokumentobjektResource;
import no.novari.flyt.archive.gateway.dispatch.model.instance.DokumentobjektDto;
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
        dokumentobjektDto.getVariantformat().map(Link::with).ifPresent(dokumentobjektResource::addVariantFormat);
        dokumentobjektDto.getFilformat().map(Link::with).ifPresent(dokumentobjektResource::addFilformat);
        dokumentobjektDto.getFormat().ifPresent(dokumentobjektResource::setFormat);
        dokumentobjektDto.getFileId()
                .map(fileArchiveLinkPerFileId::get)
                .ifPresent(dokumentobjektResource::addReferanseDokumentfil);
        return dokumentobjektResource;
    }

}
