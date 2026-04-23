package no.novari.flyt.archive.gateway.dispatch.mapping

import no.novari.flyt.archive.gateway.dispatch.model.instance.AdresseDto
import no.novari.flyt.archive.gateway.dispatch.model.instance.KontaktinformasjonDto
import no.novari.flyt.archive.gateway.dispatch.model.instance.KorrespondansepartDto
import no.novari.flyt.archive.gateway.dispatch.model.instance.SkjermingDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class KorrespondansepartMappingServiceTest {
    private lateinit var korrespondansepartMappingService: KorrespondansepartMappingService
    private lateinit var adresseMappingService: AdresseMappingService
    private lateinit var kontaktinformasjonMappingService: KontaktinformasjonMappingService
    private lateinit var skjermingMappingService: SkjermingMappingService

    @BeforeEach
    fun setup() {
        adresseMappingService = mock()
        kontaktinformasjonMappingService = mock()
        skjermingMappingService = mock()
        korrespondansepartMappingService =
            KorrespondansepartMappingService(
                adresseMappingService,
                kontaktinformasjonMappingService,
                skjermingMappingService,
            )
    }

    @Test
    fun testToKorrespondansepartResource() {
        val korrespondansepartDto =
            KorrespondansepartDto
                .builder()
                .korrespondanseparttype("type")
                .fodselsnummer("123456789")
                .organisasjonsnummer("987654321")
                .korrespondansepartNavn("name")
                .kontaktperson("contactPerson")
                .adresse(AdresseDto.builder().build())
                .kontaktinformasjon(KontaktinformasjonDto.builder().build())
                .skjerming(SkjermingDto.builder().build())
                .build()

        val korrespondansepartResource =
            korrespondansepartMappingService
                .toKorrespondansepartResource(
                    listOf(korrespondansepartDto),
                ).first()

        assertThat(korrespondansepartResource.fodselsnummer).isEqualTo("123456789")
        assertThat(korrespondansepartResource.organisasjonsnummer).isEqualTo("987654321")
        assertThat(korrespondansepartResource.korrespondansepartNavn).isEqualTo("name")
        assertThat(korrespondansepartResource.kontaktperson).isEqualTo("contactPerson")

        verify(adresseMappingService).toAdresseResource(korrespondansepartDto.adresse!!)
        verify(kontaktinformasjonMappingService).toKontaktinformasjon(korrespondansepartDto.kontaktinformasjon)
        verify(skjermingMappingService).toSkjermingResource(korrespondansepartDto.skjerming)
    }
}
