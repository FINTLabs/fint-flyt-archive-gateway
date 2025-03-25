package no.fintlabs.flyt.gateway.application.archive.dispatch.model.instance;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Optional;

@Builder
@Jacksonized
public class JournalpostDto {
    private final String tittel;
    private final String offentligTittel;
    private final String journalposttype;
    private final String administrativEnhet;
    private final String saksbehandler;
    private final String journalstatus;
    private final String tilgangsgruppe;
    private final @Valid SkjermingDto skjerming;
    private final Collection<@NotNull @Valid KorrespondansepartDto> korrespondansepart;
    private final Collection<@NotNull @Valid DokumentbeskrivelseDto> dokumentbeskrivelse;

    public Optional<String> getTittel() {
        return Optional.ofNullable(tittel);
    }

    public Optional<String> getOffentligTittel() {
        return Optional.ofNullable(offentligTittel);
    }

    public Optional<String> getJournalstatus() {
        return Optional.ofNullable(journalstatus);
    }

    public Optional<String> getTilgangsgruppe() {
        return Optional.ofNullable(tilgangsgruppe);
    }

    public Optional<String> getSaksbehandler() {
        return Optional.ofNullable(saksbehandler);
    }

    public Optional<String> getJournalposttype() {
        return Optional.ofNullable(journalposttype);
    }

    public Optional<String> getAdministrativEnhet() {
        return Optional.ofNullable(administrativEnhet);
    }

    public Optional<SkjermingDto> getSkjerming() {
        return Optional.ofNullable(skjerming);
    }

    public Optional<Collection<KorrespondansepartDto>> getKorrespondansepart() {
        return Optional.ofNullable(korrespondansepart);
    }

    public Optional<Collection<DokumentbeskrivelseDto>> getDokumentbeskrivelse() {
        return Optional.ofNullable(dokumentbeskrivelse);
    }

}
