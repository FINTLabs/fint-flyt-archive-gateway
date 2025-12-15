package no.novari.flyt.archive.gateway.dispatch.model.instance;

import jakarta.validation.Valid;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.util.Optional;

@Builder
@Jacksonized
public class KorrespondansepartDto {
    private final String korrespondanseparttype;
    private final String organisasjonsnummer;
    private final String fodselsnummer;
    private final String korrespondansepartNavn;
    private final String kontaktperson;
    private final @Valid AdresseDto adresse;
    private final @Valid KontaktinformasjonDto kontaktinformasjon;
    private final @Valid SkjermingDto skjerming;

    public Optional<String> getKorrespondanseparttype() {
        return Optional.ofNullable(korrespondanseparttype);
    }

    public Optional<String> getFodselsnummer() {
        return Optional.ofNullable(fodselsnummer);
    }

    public Optional<String> getOrganisasjonsnummer() {
        return Optional.ofNullable(organisasjonsnummer);
    }

    public Optional<String> getKorrespondansepartNavn() {
        return Optional.ofNullable(korrespondansepartNavn);
    }

    public Optional<AdresseDto> getAdresse() {
        return Optional.ofNullable(adresse);
    }

    public Optional<String> getKontaktperson() {
        return Optional.ofNullable(kontaktperson);
    }

    public Optional<KontaktinformasjonDto> getKontaktinformasjon() {
        return Optional.ofNullable(kontaktinformasjon);
    }

    public Optional<SkjermingDto> getSkjerming() {
        return Optional.ofNullable(skjerming);
    }

}
