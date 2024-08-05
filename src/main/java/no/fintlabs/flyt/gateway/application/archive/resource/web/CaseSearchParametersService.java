package no.fintlabs.flyt.gateway.application.archive.resource.web;

import no.fint.model.felles.basisklasser.Begrep;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.arkiv.kodeverk.SaksmappetypeResource;
import no.fint.model.resource.arkiv.kodeverk.TilgangsrestriksjonResource;
import no.fint.model.resource.arkiv.noark.AdministrativEnhetResource;
import no.fint.model.resource.arkiv.noark.ArkivdelResource;
import no.fint.model.resource.arkiv.noark.KlassifikasjonssystemResource;
import no.fintlabs.cache.FintCache;
import no.fintlabs.flyt.gateway.application.archive.dispatch.model.instance.CaseSearchParametersDto;
import no.fintlabs.flyt.gateway.application.archive.dispatch.model.instance.KlasseDto;
import no.fintlabs.flyt.gateway.application.archive.dispatch.model.instance.SakDto;
import no.fintlabs.flyt.gateway.application.archive.dispatch.model.instance.SkjermingDto;
import org.springframework.stereotype.Service;

import java.util.StringJoiner;

@Service
public class CaseSearchParametersService {

    private final FintCache<String, ArkivdelResource> arkivdelResourceCache;
    private final FintCache<String, AdministrativEnhetResource> administrativEnhetResourceCache;
    private final FintCache<String, TilgangsrestriksjonResource> tilgangsrestriksjonResourceCache;
    private final FintCache<String, SaksmappetypeResource> saksmappetypeResourceCache;
    private final FintCache<String, KlassifikasjonssystemResource> klassifikasjonssystemResourceCache;

    public CaseSearchParametersService(
            FintCache<String, ArkivdelResource> arkivdelResourceCache,
            FintCache<String, AdministrativEnhetResource> administrativEnhetResourceCache,
            FintCache<String, TilgangsrestriksjonResource> tilgangsrestriksjonResourceCache,
            FintCache<String, SaksmappetypeResource> saksmappetypeResourceCache,
            FintCache<String, KlassifikasjonssystemResource> klassifikasjonssystemResourceCache
    ) {
        this.arkivdelResourceCache = arkivdelResourceCache;
        this.administrativEnhetResourceCache = administrativEnhetResourceCache;
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
        if (caseSearchParametersDto.isAdministrativEnhet()) {
            sakDto.getAdministrativEnhet()
                    .map(administrativEnhetResourceCache::get)
                    .map(AdministrativEnhetResource::getSystemId)
                    .map(Identifikator::getIdentifikatorverdi)
                    .map(value -> createFilterLine("administrativenhet", value))
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

                        KlasseDto klasseDtoMatchingRekkefolge = sakDto.getKlasse()
                                .flatMap(
                                        klasseDtos -> klasseDtos.stream()
                                                .filter(
                                                        klasseDto -> klasseDto.getRekkefolge()
                                                                .map(rekkefolge::equals)
                                                                .orElse(false)
                                                )
                                                .findFirst()
                                )
                                .orElseThrow(IllegalStateException::new);

                        //TODO: handle exception. map to caseSearchResult?

                        if (caseSearchParametersDto.getKlasseringKlassifikasjonssystem()) {
                            klasseDtoMatchingRekkefolge.getKlassifikasjonssystem()
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
                            klasseDtoMatchingRekkefolge.getKlasseId()
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
            case 1 -> "primar";
            case 2 -> "sekundar";
            case 3 -> "tertiar";
            default -> throw new IllegalArgumentException("Rekkefolge must be 1, 2 or 3");
        };
        return "klassifikasjon/" + klassifikasjonName + "/";
    }

}
