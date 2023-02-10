package no.fintlabs.mapping;

import no.fint.model.felles.kompleksedatatyper.Kontaktinformasjon;
import no.fintlabs.model.instance.KontaktinformasjonDto;
import org.springframework.stereotype.Service;

@Service
public class KontaktinformasjonMappingService {

    public Kontaktinformasjon toKontaktinformasjon(KontaktinformasjonDto kontaktinformasjonDto) {
        if (kontaktinformasjonDto == null) {
            return null;
        }
        Kontaktinformasjon kontaktinformasjon = new Kontaktinformasjon();
        kontaktinformasjonDto.getEpostadresse().ifPresent(kontaktinformasjon::setEpostadresse);
        kontaktinformasjonDto.getTelefonnummer().ifPresent(kontaktinformasjon::setTelefonnummer);
        kontaktinformasjonDto.getMobiltelefonnummer().ifPresent(kontaktinformasjon::setMobiltelefonnummer);
        return kontaktinformasjon;
    }

}
