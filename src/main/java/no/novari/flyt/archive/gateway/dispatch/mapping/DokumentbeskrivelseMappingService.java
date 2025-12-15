package no.novari.flyt.archive.gateway.dispatch.mapping;

import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.DokumentbeskrivelseResource;
import no.novari.flyt.archive.gateway.dispatch.model.instance.DokumentbeskrivelseDto;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DokumentbeskrivelseMappingService {

    private final DokumentObjektMappingService dokumentObjektMappingService;
    private final SkjermingMappingService skjermingMappingService;

    public DokumentbeskrivelseMappingService(
            DokumentObjektMappingService dokumentObjektMappingService,
            SkjermingMappingService skjermingMappingService
    ) {
        this.dokumentObjektMappingService = dokumentObjektMappingService;
        this.skjermingMappingService = skjermingMappingService;
    }

    public List<DokumentbeskrivelseResource> toDokumentbeskrivelseResource(
            Collection<DokumentbeskrivelseDto> dokumentbeskrivelseDto,
            Map<UUID, Link> fileArchiveLinkPerFileId
    ) {
        return dokumentbeskrivelseDto.stream()
                .map(dbDto -> toDokumentbeskrivelseResource(dbDto, fileArchiveLinkPerFileId))
                .toList();
    }

    public DokumentbeskrivelseResource toDokumentbeskrivelseResource(
            DokumentbeskrivelseDto dokumentbeskrivelseDto,
            Map<UUID, Link> fileArchiveLinkPerFileId
    ) {
        DokumentbeskrivelseResource dokumentbeskrivelseResource = new DokumentbeskrivelseResource();
        dokumentbeskrivelseDto.getTittel().ifPresent(dokumentbeskrivelseResource::setTittel);
        dokumentbeskrivelseDto.getDokumentType().map(Link::with).ifPresent(dokumentbeskrivelseResource::addDokumentType);
        dokumentbeskrivelseDto.getTilknyttetRegistreringSom().map(Link::with).ifPresent(dokumentbeskrivelseResource::addTilknyttetRegistreringSom);
        dokumentbeskrivelseDto.getDokumentstatus().map(Link::with).ifPresent(dokumentbeskrivelseResource::addDokumentstatus);
        dokumentbeskrivelseDto.getDokumentobjekt()
                .map(dokumentobjektDtos -> dokumentObjektMappingService.toDokumentobjektResource(
                        dokumentobjektDtos,
                        fileArchiveLinkPerFileId
                ))
                .ifPresent(dokumentbeskrivelseResource::setDokumentobjekt);
        dokumentbeskrivelseDto.getSkjerming()
                .map(skjermingMappingService::toSkjermingResource)
                .ifPresent(dokumentbeskrivelseResource::setSkjerming);
        return dokumentbeskrivelseResource;
    }

}
