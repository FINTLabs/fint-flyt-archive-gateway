package no.fintlabs.flyt.gateway.application.archive.dispatch.model.instance;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import javax.validation.Valid;
import java.util.Optional;

@Builder
@Jacksonized
public class PartDto {
    private final String partNavn;
    private final String partRolle;
    private final String kontaktperson;
    private final String organisasjonsnummer;
    private final String fodselsnummer;
    private final @Valid AdresseDto adresse;
    private final @Valid KontaktinformasjonDto kontaktinformasjon;

    public Optional<String> getPartNavn() {
        return Optional.ofNullable(partNavn);
    }

    public Optional<String> getPartRolle() {
        return Optional.ofNullable(partRolle);
    }

    public Optional<String> getKontaktperson() {
        return Optional.ofNullable(kontaktperson);
    }

    public Optional<String> getOrganisasjonsnummer() { return Optional.ofNullable(organisasjonsnummer); }

    public Optional<String> getFodselsnummer() { return Optional.ofNullable(fodselsnummer); }


    public Optional<AdresseDto> getAdresse() {
        return Optional.ofNullable(adresse);
    }

    public Optional<KontaktinformasjonDto> getKontaktinformasjon() {
        return Optional.ofNullable(kontaktinformasjon);
    }
}
