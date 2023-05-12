package no.fintlabs.web.archive;

import no.fint.model.felles.basisklasser.Begrep;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.arkiv.kodeverk.SaksmappetypeResource;
import no.fint.model.resource.arkiv.kodeverk.TilgangsrestriksjonResource;
import no.fint.model.resource.arkiv.noark.ArkivdelResource;
import no.fint.model.resource.arkiv.noark.KlassifikasjonssystemResource;
import no.fintlabs.cache.FintCache;
import no.fintlabs.model.instance.CaseSearchParametersDto;
import no.fintlabs.model.instance.KlasseDto;
import no.fintlabs.model.instance.SakDto;
import no.fintlabs.model.instance.SkjermingDto;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.StringJoiner;

@Service
public class CaseSearchParametersService {

    private final FintCache<String, ArkivdelResource> arkivdelResourceCache;
    private final FintCache<String, TilgangsrestriksjonResource> tilgangsrestriksjonResourceCache;
    private final FintCache<String, SaksmappetypeResource> saksmappetypeResourceCache;
    private final FintCache<String, KlassifikasjonssystemResource> klassifikasjonssystemResourceCache;

    public CaseSearchParametersService(
            FintCache<String, ArkivdelResource> arkivdelResourceCache,
            FintCache<String, TilgangsrestriksjonResource> tilgangsrestriksjonResourceCache,
            FintCache<String, SaksmappetypeResource> saksmappetypeResourceCache,
            FintCache<String, KlassifikasjonssystemResource> klassifikasjonssystemResourceCache
    ) {
        this.arkivdelResourceCache = arkivdelResourceCache;
        this.tilgangsrestriksjonResourceCache = tilgangsrestriksjonResourceCache;
        this.saksmappetypeResourceCache = saksmappetypeResourceCache;
        this.klassifikasjonssystemResourceCache = klassifikasjonssystemResourceCache;
    }

    public String createFilterQueryParamValue(SakDto sakDto, CaseSearchParametersDto caseSearchParametersDto) {
        StringJoiner filterJoiner = new StringJoiner(" and ");

        if (caseSearchParametersDto.isArkivdel()) {
            sakDto.getArkivdel()
                    .map(arkivdelResourceCache::get)
                    .map(ArkivdelResource::getSystemId)
                    .map(Identifikator::getIdentifikatorverdi)
                    .map(value -> createFilterLine("arkivdel", value))
                    .ifPresent(filterJoiner::add);
        }
        if (caseSearchParametersDto.isTilgangsrestriksjon()) {
            sakDto.getSkjerming()
                    .flatMap(SkjermingDto::getTilgangsrestriksjon)
                    .map(tilgangsrestriksjonResourceCache::get)
                    .map(Begrep::getSystemId)
                    .map(Identifikator::getIdentifikatorverdi)
                    .map(value -> createFilterLine("tilgangskode", value))
                    .ifPresent(filterJoiner::add);
        }
        if (caseSearchParametersDto.isSaksmappetype()) {
            sakDto.getSaksmappetype()
                    .map(saksmappetypeResourceCache::get)
                    .map(Begrep::getSystemId)
                    .map(Identifikator::getIdentifikatorverdi)
                    .map(value -> createFilterLine("saksmappetype", value))
                    .ifPresent(filterJoiner::add);
        }
        if (caseSearchParametersDto.isTittel()) {
            sakDto.getTittel()
                    .map(value -> createFilterLine("tittel", value))
                    .ifPresent(filterJoiner::add);
        }
        if (caseSearchParametersDto.isKlassering()) {
            caseSearchParametersDto.getKlasseringRekkefolge()
                    .map(Integer::parseInt)
                    .ifPresent(rekkefolge -> {

                        Optional<KlasseDto> klasseDto = sakDto.getKlasse()
                                .map(klasseDtos -> klasseDtos.get(rekkefolge));

                        if (caseSearchParametersDto.getKlasseringKlassifikasjonssystem()) {
                            klasseDto
                                    .flatMap(KlasseDto::getKlassifikasjonssystem)
                                    .map(klassifikasjonssystemResourceCache::get)
                                    .map(KlassifikasjonssystemResource::getSystemId)
                                    .map(Identifikator::getIdentifikatorverdi)
                                    .map(value -> createFilterLine(
                                            createKlasseringPrefix(rekkefolge) + "ordning",
                                            value
                                    ))
                                    .ifPresent(filterJoiner::add);
                        }
                        if (caseSearchParametersDto.getKlasseringKlasseId()) {
                            klasseDto
                                    .flatMap(KlasseDto::getKlasseId)
                                    .map(klasseId -> createFilterLine(
                                            createKlasseringPrefix(rekkefolge) + "verdi",
                                            klasseId
                                    ))
                                    .ifPresent(filterJoiner::add);
                        }
                    });
        }
        return filterJoiner.toString();
    }

    private String createFilterLine(String name, String value) {
        return name + " eq '" + value + "'";
    }

    private String createKlasseringPrefix(int rekkefolge) {
        String klassifikasjonName = switch (rekkefolge) {
            case 0 -> "primar";
            case 1 -> "sekundar";
            case 2 -> "tertiar";
            default -> throw new IllegalArgumentException("Rekkefolge must be 0, 1 or 2");
        };
        return "klassifikasjon/" + klassifikasjonName + "/";
    }

}
