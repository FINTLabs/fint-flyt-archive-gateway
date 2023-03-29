package no.fintlabs.mapping;

import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.SakResource;
import no.fintlabs.model.instance.SakDto;
import org.springframework.stereotype.Service;

@Service
public class SakMappingService {

    private final SkjermingMappingService skjermingMappingService;
    private final KlasseMappingService klasseMappingService;
    private final PartMappingService partMappingService;

    public SakMappingService(
            SkjermingMappingService skjermingMappingService,
            KlasseMappingService klasseMappingService,
            PartMappingService partMappingService) {
        this.skjermingMappingService = skjermingMappingService;
        this.klasseMappingService = klasseMappingService;
        this.partMappingService = partMappingService;
    }

    public SakResource toSakResource(SakDto sakDto) {
        if (sakDto == null) {
            return null;
        }
        SakResource sakResource = new SakResource();
        sakDto.getTittel().ifPresent(sakResource::setTittel);
        sakDto.getOffentligTittel().ifPresent(sakResource::setOffentligTittel);
        sakDto.getSaksmappetype().map(Link::with).ifPresent(sakResource::addSaksmappetype);
        sakDto.getSaksstatus().map(Link::with).ifPresent(sakResource::addSaksstatus);
        sakDto.getJournalenhet().map(Link::with).ifPresent(sakResource::addJournalenhet);
        sakDto.getAdministrativenhet().map(Link::with).ifPresent(sakResource::addAdministrativEnhet);
        sakDto.getSaksansvarlig().map(Link::with).ifPresent(sakResource::addSaksansvarlig);
        sakDto.getArkivdel().map(Link::with).ifPresent(sakResource::addArkivdel);
        sakDto.getPart()
                .map(part -> part
                        .stream()
                        .map(partMappingService::toPartResource)
                        .toList()
                )
                .ifPresent(sakResource::setPart);
        sakDto.getSkjerming()
                .map(skjermingMappingService::toSkjermingResource)
                .ifPresent(sakResource::setSkjerming);
        sakDto.getKlasse()
                .map(klasseMappingService::toKlasse)
                .ifPresent(sakResource::setKlasse);

        return sakResource;
    }

}
