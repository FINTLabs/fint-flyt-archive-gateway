package no.fintlabs.model.instance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.fint.model.resource.Link;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NySakDto {
    private String tittel;
    private String offentligTittel;
    private Link saksmappetype;
    private Link saksstatus;
    private Link journalenhet;
    private Link administrativenhet;
    private Link saksansvarlig;
    private Link arkivdel;
    private SkjermingDto skjerming;
    private List<KlasseDto> klasse;
}