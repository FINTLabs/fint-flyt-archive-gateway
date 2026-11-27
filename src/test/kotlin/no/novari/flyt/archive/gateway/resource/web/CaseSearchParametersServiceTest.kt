package no.novari.flyt.archive.gateway.resource.web

import no.novari.fint.model.felles.kompleksedatatyper.Identifikator
import no.novari.fint.model.resource.arkiv.kodeverk.SaksstatusResource
import no.novari.fint.model.resource.arkiv.kodeverk.TilgangsrestriksjonResource
import no.novari.fint.model.resource.arkiv.noark.AdministrativEnhetResource
import no.novari.fint.model.resource.arkiv.noark.ArkivdelResource
import no.novari.fint.model.resource.arkiv.noark.KlassifikasjonssystemResource
import no.novari.flyt.archive.gateway.dispatch.model.instance.CaseSearchParametersDto
import no.novari.flyt.archive.gateway.dispatch.model.instance.KlasseDto
import no.novari.flyt.archive.gateway.dispatch.model.instance.SakDto
import no.novari.flyt.archive.gateway.dispatch.model.instance.SkjermingDto
import no.novari.flyt.archive.gateway.resource.web.exceptions.KlasseOrderOutOfBoundsException
import no.novari.flyt.archive.gateway.resource.web.exceptions.SearchKlasseOrderNotFoundInCaseException
import no.novari.flyt.cache.FlytCache
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class CaseSearchParametersServiceTest {
    @Mock
    private lateinit var arkivdelResourceCache: FlytCache<String, ArkivdelResource>

    @Mock
    private lateinit var administrativEnhetResourceCache: FlytCache<String, AdministrativEnhetResource>

    @Mock
    private lateinit var tilgangsrestriksjonResourceCache: FlytCache<String, TilgangsrestriksjonResource>

    @Mock
    private lateinit var saksstatusResourceCache: FlytCache<String, SaksstatusResource>

    @Mock
    private lateinit var klassifikasjonssystemResourceCache: FlytCache<String, KlassifikasjonssystemResource>

    private lateinit var caseSearchParametersService: CaseSearchParametersService

    @BeforeEach
    fun setUp() {
        caseSearchParametersService =
            CaseSearchParametersService(
                arkivdelResourceCache,
                administrativEnhetResourceCache,
                tilgangsrestriksjonResourceCache,
                mock(),
                saksstatusResourceCache,
                klassifikasjonssystemResourceCache,
            )
    }

    @Test
    fun `creates a filter query param value for arkivdel`() {
        val arkivdel =
            ArkivdelResource().apply {
                systemId = Identifikator().apply { identifikatorverdi = "arkivdelId" }
            }
        whenever(arkivdelResourceCache.get("arkivdelKey")).thenReturn(arkivdel)

        val sakDto =
            SakDto(
                arkivdel = "arkivdelKey",
            )
        val caseSearchParametersDto =
            CaseSearchParametersDto(
                arkivdel = true,
            )

        val result = caseSearchParametersService.createFilterQueryParamValue(sakDto, caseSearchParametersDto)

        assertThat(result).isEqualTo("arkivdel eq 'arkivdelId'")
    }

    @Test
    fun `creates a filter query param value for administrativEnhet and tilgangsrestriksjon`() {
        val administrativEnhetResource =
            AdministrativEnhetResource().apply {
                systemId =
                    Identifikator().apply { identifikatorverdi = "adminEnhetId" }
            }
        val tilgangsrestriksjonResource =
            TilgangsrestriksjonResource().apply {
                systemId = Identifikator().apply { identifikatorverdi = "tilgangsId" }
            }

        whenever(administrativEnhetResourceCache.get("adminEnhetKey")).thenReturn(administrativEnhetResource)
        whenever(tilgangsrestriksjonResourceCache.get("tilgangsKey")).thenReturn(tilgangsrestriksjonResource)

        val sakDto =
            SakDto(
                administrativEnhet = "adminEnhetKey",
                skjerming =
                    SkjermingDto(
                        tilgangsrestriksjon = "tilgangsKey",
                    ),
            )
        val caseSearchParametersDto =
            CaseSearchParametersDto(
                administrativEnhet = true,
                tilgangsrestriksjon = true,
            )

        val result = caseSearchParametersService.createFilterQueryParamValue(sakDto, caseSearchParametersDto)

        assertThat(result).isEqualTo("administrativenhet eq 'adminEnhetId' and tilgangskode eq 'tilgangsId'")
    }

    @Test
    fun `creates a filter query param value for klassering klassifikasjonssystem`() {
        val klassifikasjonssystemResource =
            KlassifikasjonssystemResource().apply {
                systemId =
                    Identifikator().apply { identifikatorverdi = "klassifikasjonssystemId" }
            }
        whenever(
            klassifikasjonssystemResourceCache.get("klassifikasjonssystemKey"),
        ).thenReturn(klassifikasjonssystemResource)

        val sakDto =
            SakDto(
                klasse =
                    listOf(
                        KlasseDto(
                            rekkefolge = 1,
                            klassifikasjonssystem = "klassifikasjonssystemKey",
                        ),
                    ),
            )
        val caseSearchParametersDto =
            CaseSearchParametersDto(
                klassering = true,
                klasseringRekkefolge = "1",
                klasseringKlassifikasjonssystem = true,
            )

        val result = caseSearchParametersService.createFilterQueryParamValue(sakDto, caseSearchParametersDto)

        assertThat(result).isEqualTo("klassifikasjon/primar/ordning eq 'klassifikasjonssystemId'")
    }

    @Test
    fun `given klassering rekkefolge out of bounds, throws KlasseOrderOutOfBoundsException`() {
        val sakDto =
            SakDto(
                klasse =
                    listOf(
                        KlasseDto(
                            rekkefolge = 0,
                            klasseId = "klasseId",
                        ),
                    ),
            )
        val caseSearchParametersDto =
            CaseSearchParametersDto(
                klassering = true,
                klasseringRekkefolge = "0",
                klasseringKlasseId = true,
            )

        assertThatThrownBy { caseSearchParametersService.createFilterQueryParamValue(sakDto, caseSearchParametersDto) }
            .isInstanceOf(KlasseOrderOutOfBoundsException::class.java)
            .hasMessage("Rekkefolge=0 is out of bounds. Rekkefolge must be 1, 2 or 3.")
    }

    @Test
    fun `given a missing klassering order, throws SearchKlasseOrderNotFoundInCaseException`() {
        val sakDto =
            SakDto(
                klasse =
                    listOf(
                        KlasseDto(
                            rekkefolge = 1,
                            klasseId = "klasseId",
                        ),
                    ),
            )
        val caseSearchParametersDto =
            CaseSearchParametersDto(
                klassering = true,
                klasseringRekkefolge = "2",
                klasseringKlasseId = true,
            )

        assertThatThrownBy { caseSearchParametersService.createFilterQueryParamValue(sakDto, caseSearchParametersDto) }
            .isInstanceOf(SearchKlasseOrderNotFoundInCaseException::class.java)
    }
}
