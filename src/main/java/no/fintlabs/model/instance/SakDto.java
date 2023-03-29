package no.fintlabs.model.instance;

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
    private final String administrativenhet;
    private final String saksansvarlig;
    private final String arkivdel;
    private final String saksstatus;
    private final List<PartDto> part;
    private final SkjermingDto skjerming;
    private final List<KlasseDto> klasse;
    private final List<JournalpostDto> journalpost;

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

    public Optional<String> getJournalenhet() {
        return Optional.ofNullable(journalenhet);
    }

    public Optional<String> getAdministrativenhet() {
        return Optional.ofNullable(administrativenhet);
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
