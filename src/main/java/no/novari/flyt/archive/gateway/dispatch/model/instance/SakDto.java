package no.novari.flyt.archive.gateway.dispatch.model.instance;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.Optional;

@Builder
@Jacksonized
public class SakDto {
    private final String tittel;
    private final String offentligTittel;
    private final String saksmappetype;
    private final String journalenhet;
    private final String administrativEnhet;
    private final String saksansvarlig;
    private final String arkivdel;
    private final String saksstatus;
    private final String tilgangsgruppe;
    private final List<@NotNull @Valid PartDto> part;
    private final @Valid SkjermingDto skjerming;
    private final List<@NotNull @Valid KlasseDto> klasse;
    private final List<@NotNull @Valid JournalpostDto> journalpost;

    public Optional<String> getTittel() {
        return Optional.ofNullable(tittel);
    }

    public Optional<String> getOffentligTittel() {
        return Optional.ofNullable(offentligTittel);
    }

    public Optional<String> getSaksmappetype() {
        return Optional.ofNullable(saksmappetype);
    }

    public Optional<String> getSaksstatus() {
        return Optional.ofNullable(saksstatus);
    }

    public Optional<String> getTilgangsgruppe() {
        return Optional.ofNullable(tilgangsgruppe);
    }

    public Optional<String> getJournalenhet() {
        return Optional.ofNullable(journalenhet);
    }

    public Optional<String> getAdministrativEnhet() {
        return Optional.ofNullable(administrativEnhet);
    }

    public Optional<String> getSaksansvarlig() {
        return Optional.ofNullable(saksansvarlig);
    }

    public Optional<String> getArkivdel() {
        return Optional.ofNullable(arkivdel);
    }

    public Optional<SkjermingDto> getSkjerming() {
        return Optional.ofNullable(skjerming);
    }

    public Optional<List<KlasseDto>> getKlasse() {
        return Optional.ofNullable(klasse);
    }

    public Optional<List<PartDto>> getPart() {
        return Optional.ofNullable(part);
    }

    public Optional<List<JournalpostDto>> getJournalpost() {
        return Optional.ofNullable(journalpost);
    }

}
