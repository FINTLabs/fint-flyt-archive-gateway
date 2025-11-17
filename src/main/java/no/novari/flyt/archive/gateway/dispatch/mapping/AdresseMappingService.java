package no.novari.flyt.archive.gateway.dispatch.mapping;

import no.fint.model.resource.felles.kompleksedatatyper.AdresseResource;
import no.novari.flyt.archive.gateway.dispatch.model.instance.AdresseDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class AdresseMappingService {

    public AdresseResource toAdresseResource(AdresseDto adresseDto) {
        AdresseResource adresseResource = new AdresseResource();
        adresseDto.getAdresselinje().map(ArrayList::new).ifPresent(adresseResource::setAdresselinje);
        adresseDto.getPostnummer().ifPresent(adresseResource::setPostnummer);
        adresseDto.getPoststed().ifPresent(adresseResource::setPoststed);
        return adresseResource;
    }

}
