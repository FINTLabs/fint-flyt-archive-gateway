package no.novari.flyt.archive.gateway.dispatch.mapping

import no.novari.fint.model.felles.kompleksedatatyper.Kontaktinformasjon
import no.novari.fint.model.resource.felles.kompleksedatatyper.AdresseResource
import no.novari.flyt.archive.gateway.dispatch.model.instance.AdresseDto
import no.novari.flyt.archive.gateway.dispatch.model.instance.KontaktinformasjonDto
import no.novari.flyt.archive.gateway.dispatch.model.instance.PartDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class PartMappingServiceTest {
    @Mock
    private lateinit var kontaktinformasjonMappingService: KontaktinformasjonMappingService

    @Mock
    private lateinit var adresseMappingService: AdresseMappingService

    @InjectMocks
    private lateinit var partMappingService: PartMappingService

    @Test
    fun testToPartResourceNullDto() {
        assertThat(partMappingService.toPartResource(null)).isNull()
    }

    @Test
    fun testToPartResource() {
        val partDto =
            PartDto
                .builder()
                .partNavn("Test Navn")
                .partRolle("Test Rolle")
                .kontaktperson("Test Person")
                .fodselsnummer("123456789")
                .organisasjonsnummer("987654321")
                .adresse(
                    AdresseDto
                        .builder()
                        .adresselinje(emptyList())
                        .postnummer("1234")
                        .poststed("Test Sted")
                        .build(),
                ).kontaktinformasjon(
                    KontaktinformasjonDto
                        .builder()
                        .epostadresse("test@test.com")
                        .telefonnummer("1234567890")
                        .mobiltelefonnummer("0987654321")
                        .build(),
                ).build()

        val kontaktinformasjon = Kontaktinformasjon()
        val adresseResource = AdresseResource()

        whenever(
            kontaktinformasjonMappingService.toKontaktinformasjon(partDto.kontaktinformasjon),
        ).thenReturn(kontaktinformasjon)
        whenever(adresseMappingService.toAdresseResource(partDto.adresse!!)).thenReturn(adresseResource)

        val partResource = partMappingService.toPartResource(partDto)!!

        assertThat(partResource.partNavn).isEqualTo("Test Navn")
        assertThat(partResource.partRolle.first().href).isEqualTo("Test Rolle")
        assertThat(partResource.kontaktperson).isEqualTo("Test Person")
        assertThat(partResource.fodselsnummer).isEqualTo("123456789")
        assertThat(partResource.organisasjonsnummer).isEqualTo("987654321")
        assertThat(partResource.adresse).isEqualTo(adresseResource)
        assertThat(partResource.kontaktinformasjon).isEqualTo(kontaktinformasjon)
    }
}
