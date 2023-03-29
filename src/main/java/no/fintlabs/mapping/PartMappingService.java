package no.fintlabs.mapping;

import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.PartResource;
import no.fintlabs.model.instance.PartDto;
import org.springframework.stereotype.Service;

@Service
public class PartMappingService {

    private final KontaktinformasjonMappingService kontaktinformasjonMappingService;
    private final AdresseMappingService adresseMappingService;

    public PartMappingService(
            KontaktinformasjonMappingService kontaktinformasjonMappingService,
            AdresseMappingService adresseMappingService
    ) {
        this.kontaktinformasjonMappingService = kontaktinformasjonMappingService;
        this.adresseMappingService = adresseMappingService;
    }

    public PartResource toPartResource(PartDto partDto) {
        if (partDto == null) {
            return null;
        }
        PartResource partResource = new PartResource();
        partDto.getPartNavn().ifPresent(partResource::setPartNavn);
        partDto.getPartRolle().map(Link::with).ifPresent(partResource::addPartRolle);
        partDto.getKontaktperson().ifPresent(partResource::setKontaktperson);
        partDto.getAdresse()
                .map(adresseMappingService::toAdresseResource)
                .ifPresent(partResource::setAdresse);
        partDto.getKontaktinformasjon()
                .map(kontaktinformasjonMappingService::toKontaktinformasjon)
                .ifPresent(partResource::setKontaktinformasjon);
        return partResource;
    }

}
