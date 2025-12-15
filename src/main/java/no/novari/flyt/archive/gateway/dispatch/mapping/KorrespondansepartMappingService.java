package no.novari.flyt.archive.gateway.dispatch.mapping;

import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.KorrespondansepartResource;
import no.novari.flyt.archive.gateway.dispatch.model.instance.KorrespondansepartDto;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class KorrespondansepartMappingService {

    private final AdresseMappingService adresseMappingService;
    private final KontaktinformasjonMappingService kontaktinformasjonMappingService;
    private final SkjermingMappingService skjermingMappingService;

    public KorrespondansepartMappingService(
            AdresseMappingService adresseMappingService,
            KontaktinformasjonMappingService kontaktinformasjonMappingService,
            SkjermingMappingService skjermingMappingService
    ) {
        this.adresseMappingService = adresseMappingService;
        this.kontaktinformasjonMappingService = kontaktinformasjonMappingService;
        this.skjermingMappingService = skjermingMappingService;
    }

    public List<KorrespondansepartResource> toKorrespondansepartResource(Collection<KorrespondansepartDto> korrespondansepartDto) {
        return korrespondansepartDto.stream().map(this::toKorrespondansepartResource).toList();
    }

    public KorrespondansepartResource toKorrespondansepartResource(KorrespondansepartDto korrespondansepartDto) {
        KorrespondansepartResource korrespondansepartResource = new KorrespondansepartResource();

        korrespondansepartDto.getFodselsnummer().ifPresent(korrespondansepartResource::setFodselsnummer);
        korrespondansepartDto.getOrganisasjonsnummer().ifPresent(korrespondansepartResource::setOrganisasjonsnummer);
        korrespondansepartDto.getKorrespondansepartNavn().ifPresent(korrespondansepartResource::setKorrespondansepartNavn);

        korrespondansepartDto.getAdresse()
                .map(adresseMappingService::toAdresseResource)
                .ifPresent(korrespondansepartResource::setAdresse);

        korrespondansepartDto.getKorrespondanseparttype().map(Link::with).ifPresent(korrespondansepartResource::addKorrespondanseparttype);
        korrespondansepartDto.getKontaktperson().ifPresent(korrespondansepartResource::setKontaktperson);

        korrespondansepartDto.getKontaktinformasjon()
                .map(kontaktinformasjonMappingService::toKontaktinformasjon)
                .ifPresent(korrespondansepartResource::setKontaktinformasjon);

        korrespondansepartDto.getSkjerming()
                .map(skjermingMappingService::toSkjermingResource)
                .ifPresent(korrespondansepartResource::setSkjerming);

        return korrespondansepartResource;
    }
}
