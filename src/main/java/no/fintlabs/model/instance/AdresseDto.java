package no.fintlabs.model.instance;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Optional;

@Builder
@Jacksonized
public class AdresseDto {
    private final Collection<@NotNull String> adresselinje;
    private final String postnummer;
    private final String poststed;

    public Optional<Collection<String>> getAdresselinje() {
        return Optional.ofNullable(adresselinje);
    }

    public Optional<String> getPostnummer() {
        return Optional.ofNullable(postnummer);
    }

    public Optional<String> getPoststed() {
        return Optional.ofNullable(poststed);
    }

}