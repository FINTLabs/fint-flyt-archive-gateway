package no.fintlabs.flyt.gateway.application.archive.resource.web;

import no.fint.model.resource.arkiv.kodeverk.SaksmappetypeResource;
import no.fint.model.resource.arkiv.kodeverk.TilgangsrestriksjonResource;
import no.fint.model.resource.arkiv.noark.AdministrativEnhetResource;
import no.fint.model.resource.arkiv.noark.ArkivdelResource;
import no.fint.model.resource.arkiv.noark.KlassifikasjonssystemResource;
import no.fintlabs.cache.FintCache;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;


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


    // givenNoCaseSearchParametersShouldReturnEmptyString

    // givenAllCaseSearchParametersShouldReturnStringWithAllParameters

    // givenAllCaseSearchParametersWhereCacheIsEmptyShouldReturnEmptyString

    // givenOneCaseSearchParameterShouldReturnStringWithOneParameter


}