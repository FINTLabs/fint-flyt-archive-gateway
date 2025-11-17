package no.novari.flyt.archive.gateway.dispatch.model.instance;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.util.Optional;

@Getter
@Builder
@Jacksonized
public class CaseSearchParametersDto {
    private final boolean arkivdel;
    private final boolean administrativEnhet;
    private final boolean tilgangsrestriksjon;
    private final boolean saksmappetype;
    private final boolean saksstatus;
    private final boolean tittel;
    private final boolean klassering;
    private final String klasseringRekkefolge;
    private final Boolean klasseringKlassifikasjonssystem;
    private final Boolean klasseringKlasseId;

    public Optional<String> getKlasseringRekkefolge() {
        return Optional.ofNullable(klasseringRekkefolge);
    }

    public boolean getKlasseringKlassifikasjonssystem() {
        return Optional.ofNullable(klasseringKlassifikasjonssystem).orElse(false);
    }

    public boolean getKlasseringKlasseId() {
        return Optional.ofNullable(klasseringKlasseId).orElse(false);
    }

}
