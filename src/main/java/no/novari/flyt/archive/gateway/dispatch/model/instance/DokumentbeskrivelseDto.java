package no.novari.flyt.archive.gateway.dispatch.model.instance;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.util.Collection;
import java.util.Optional;

@Builder
@Jacksonized
public class DokumentbeskrivelseDto {
    private final String tittel;
    private final String dokumentstatus;
    private final String dokumentType;
    private final String tilknyttetRegistreringSom;
    private final Collection<@NotNull @Valid DokumentobjektDto> dokumentobjekt;
    private final @Valid SkjermingDto skjerming;

    public Optional<String> getTittel() {
        return Optional.ofNullable(tittel);
    }

    public Optional<String> getDokumentType() {
        return Optional.ofNullable(dokumentType);
    }

    public Optional<String> getTilknyttetRegistreringSom() {
        return Optional.ofNullable(tilknyttetRegistreringSom);
    }

    public Optional<String> getDokumentstatus() {
        return Optional.ofNullable(dokumentstatus);
    }

    public Optional<Collection<DokumentobjektDto>> getDokumentobjekt() {
        return Optional.ofNullable(dokumentobjekt);
    }

    public Optional<SkjermingDto> getSkjerming() {
        return Optional.ofNullable(skjerming);
    }

}
