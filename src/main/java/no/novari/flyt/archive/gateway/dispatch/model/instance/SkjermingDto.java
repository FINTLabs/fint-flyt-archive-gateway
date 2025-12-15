package no.novari.flyt.archive.gateway.dispatch.model.instance;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.util.Optional;

@Builder
@Jacksonized
public class SkjermingDto {
    private final String tilgangsrestriksjon;
    private final String skjermingshjemmel;

    public Optional<String> getTilgangsrestriksjon() {
        return Optional.ofNullable(tilgangsrestriksjon);
    }

    public Optional<String> getSkjermingshjemmel() {
        return Optional.ofNullable(skjermingshjemmel);
    }

}
