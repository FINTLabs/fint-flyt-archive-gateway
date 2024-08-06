package no.fintlabs.flyt.gateway.application.archive.resource.web;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseSearchParametersServiceTest {

    @Mock
    private FintCache<String, ArkivdelResource> arkivdelResourceCache;

    @Mock
    private FintCache<String, AdministrativEnhetResource> administrativEnhetResourceCache;

    @Mock
    private FintCache<String, TilgangsrestriksjonResource> tilgangsrestriksjonResourceCache;

    @Mock
    private FintCache<String, SaksmappetypeResource> saksmappetypeResourceCache;

    @Mock
    private FintCache<String, KlassifikasjonssystemResource> klassifikasjonssystemResourceCache;

    @Mock
    private SakDto sakDto;

    private CaseSearchParametersService caseSearchParametersService;

    private CaseSearchParametersDto.CaseSearchParametersDtoBuilder caseSearchParametersDtoBuilder;

    @BeforeEach
    void setUp() {
        caseSearchParametersService = new CaseSearchParametersService(
                arkivdelResourceCache,
                administrativEnhetResourceCache,
                tilgangsrestriksjonResourceCache,
                saksmappetypeResourceCache,
                klassifikasjonssystemResourceCache
        );
        caseSearchParametersDtoBuilder = CaseSearchParametersDto.builder();
    }

    @Test
    void shouldCreateFilterQueryParamValueForArkivdel() {
        CaseSearchParametersDto caseSearchParametersDto = caseSearchParametersDtoBuilder.arkivdel(true).build();

        Identifikator identifikator = new Identifikator();
        identifikator.setIdentifikatorverdi("arkivdelId");

        ArkivdelResource arkivdelResource = new ArkivdelResource();
        arkivdelResource.setSystemId(identifikator);

        when(sakDto.getArkivdel()).thenReturn(Optional.of("arkivdelKey"));
        when(arkivdelResourceCache.get("arkivdelKey")).thenReturn(arkivdelResource);

        String result = caseSearchParametersService.createFilterQueryParamValue(sakDto, caseSearchParametersDto);

        assertEquals("arkivdel eq 'arkivdelId'", result);
    }

    @Test
    void shouldCreateFilterQueryParamValueForAdministrativEnhetAndTilgangsrestriksjon() {
        CaseSearchParametersDto caseSearchParametersDto =
                caseSearchParametersDtoBuilder
                        .administrativEnhet(true)
                        .tilgangsrestriksjon(true)
                        .build();

        Identifikator administrativEnhetIdentifikator = new Identifikator();
        administrativEnhetIdentifikator.setIdentifikatorverdi("adminEnhetId");

        AdministrativEnhetResource administrativEnhetResource = new AdministrativEnhetResource();
        administrativEnhetResource.setSystemId(administrativEnhetIdentifikator);

        Identifikator tilgangsrestriksjonIdentifikator = new Identifikator();
        tilgangsrestriksjonIdentifikator.setIdentifikatorverdi("tilgangsId");

        TilgangsrestriksjonResource tilgangsrestriksjon = new TilgangsrestriksjonResource();
        tilgangsrestriksjon.setSystemId(tilgangsrestriksjonIdentifikator);

        SkjermingDto skjermingDto = SkjermingDto
                .builder()
                .tilgangsrestriksjon("tilgangsKey")
                .build();

        when(sakDto.getAdministrativEnhet()).thenReturn(Optional.of("adminEnhetKey"));
        when(administrativEnhetResourceCache.get("adminEnhetKey")).thenReturn(administrativEnhetResource);

        when(sakDto.getSkjerming()).thenReturn(Optional.of(skjermingDto));
        when(tilgangsrestriksjonResourceCache.get("tilgangsKey")).thenReturn(tilgangsrestriksjon);

        String result = caseSearchParametersService.createFilterQueryParamValue(sakDto, caseSearchParametersDto);

        assertEquals("administrativenhet eq 'adminEnhetId' and tilgangskode eq 'tilgangsId'", result);
    }

    @Test
    void shouldCreateFilterQueryParamValueForKlasseringKlassifikasjonssystem() {
        CaseSearchParametersDto caseSearchParametersDto = caseSearchParametersDtoBuilder
                .klassering(true)
                .klasseringRekkefolge("1")
                .klasseringKlassifikasjonssystem(true)
                .build();

        Identifikator klassifikasjonssystemIdentifikator = new Identifikator();
        klassifikasjonssystemIdentifikator.setIdentifikatorverdi("klassifikasjonssystemId");

        KlassifikasjonssystemResource klassifikasjonssystemResource = new KlassifikasjonssystemResource();
        klassifikasjonssystemResource.setSystemId(klassifikasjonssystemIdentifikator);

        KlasseDto klasseDto = KlasseDto.builder()
                .rekkefolge(1)
                .klassifikasjonssystem("klassifikasjonssystemKey")
                .build();

        when(sakDto.getKlasse()).thenReturn(Optional.of(List.of(klasseDto)));
        when(klassifikasjonssystemResourceCache.get("klassifikasjonssystemKey")).thenReturn(klassifikasjonssystemResource);

        String result = caseSearchParametersService.createFilterQueryParamValue(sakDto, caseSearchParametersDto);

        assertEquals("klassifikasjon/primar/ordning eq 'klassifikasjonssystemId'", result);
    }

    @Test
    void shouldCreateFilterQueryParamValueForKlasseringKlasseId() {
        CaseSearchParametersDto caseSearchParametersDto = caseSearchParametersDtoBuilder
                .klassering(true)
                .klasseringRekkefolge("1")
                .klasseringKlasseId(true)
                .build();

        KlasseDto klasseDto = KlasseDto.builder()
                .rekkefolge(1)
                .klasseId("klasseId")
                .build();

        when(sakDto.getKlasse()).thenReturn(Optional.of(List.of(klasseDto)));

        String result = caseSearchParametersService.createFilterQueryParamValue(sakDto, caseSearchParametersDto);

        assertEquals("klassifikasjon/primar/verdi eq 'klasseId'", result);
    }

}