package no.novari.flyt.archive.gateway.dispatch.model.instance;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.util.Optional;

@Builder
@Jacksonized
public class KlasseDto {
    private final @NotNull Integer rekkefolge;
    private final String klassifikasjonssystem;
    private final String klasseId;
    private final String tittel;
    private final @Valid SkjermingDto skjerming;

    public Optional<Integer> getRekkefolge() {
        return Optional.ofNullable(rekkefolge);
    }

    public Optional<String> getKlassifikasjonssystem() {
        return Optional.ofNullable(klassifikasjonssystem);
    }

    public Optional<String> getKlasseId() {
        return Optional.ofNullable(klasseId);
    }

    public Optional<SkjermingDto> getSkjerming() {
        return Optional.ofNullable(skjerming);
    }

    public Optional<String> getTittel() {
        return Optional.ofNullable(tittel);
    }

}
