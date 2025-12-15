package no.novari.flyt.archive.gateway.dispatch.model.instance;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.util.Optional;

@Builder
@Jacksonized
public class KontaktinformasjonDto {
    private final String epostadresse;
    private final String mobiltelefonnummer;
    private final String telefonnummer;

    public Optional<String> getEpostadresse() {
        return Optional.ofNullable(epostadresse);
    }

    public Optional<String> getTelefonnummer() {
        return Optional.ofNullable(telefonnummer);
    }

    public Optional<String> getMobiltelefonnummer() {
        return Optional.ofNullable(mobiltelefonnummer);
    }

}
