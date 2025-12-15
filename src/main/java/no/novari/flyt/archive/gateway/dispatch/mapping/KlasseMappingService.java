package no.novari.flyt.archive.gateway.dispatch.mapping;

import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.KlasseResource;
import no.novari.flyt.archive.gateway.dispatch.model.instance.KlasseDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KlasseMappingService {

    private final SkjermingMappingService skjermingMappingService;

    public KlasseMappingService(SkjermingMappingService skjermingMappingService) {
        this.skjermingMappingService = skjermingMappingService;
    }

    public List<KlasseResource> toKlasse(List<KlasseDto> klasseDtos) {
        if (klasseDtos == null) {
            return null;
        }
        return klasseDtos.stream().map(this::toKlasse).toList();
    }

    private KlasseResource toKlasse(KlasseDto klasseDto) {
        KlasseResource klasseResource = new KlasseResource();
        klasseDto.getKlasseId().ifPresent(klasseResource::setKlasseId);
        klasseDto.getSkjerming()
                .map(skjermingMappingService::toSkjermingResource)
                .ifPresent(klasseResource::setSkjerming);
        klasseDto.getTittel().ifPresent(klasseResource::setTittel);
        klasseDto.getKlassifikasjonssystem().map(Link::with).ifPresent(klasseResource::addKlassifikasjonssystem);
        klasseDto.getRekkefolge().ifPresent(klasseResource::setRekkefolge);
        return klasseResource;
    }

}
