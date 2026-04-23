package no.novari.flyt.archive.gateway.dispatch.mapping

import no.novari.flyt.archive.gateway.dispatch.model.instance.AdresseDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AdresseMappingServiceTest {
    private lateinit var adresseMappingService: AdresseMappingService

    @BeforeEach
    fun setUp() {
        adresseMappingService = AdresseMappingService()
    }

    @Test
    fun toAdresseResource() {
        val adresseLinjer = listOf("Linje 1", "Linje 2")
        val adresseDto =
            AdresseDto
                .builder()
                .adresselinje(adresseLinjer)
                .postnummer("12345")
                .poststed("testPostSted")
                .build()

        val adresseResource = adresseMappingService.toAdresseResource(adresseDto)

        assertThat(adresseResource.adresselinje).containsExactlyElementsOf(adresseLinjer)
        assertThat(adresseResource.postnummer).isEqualTo("12345")
        assertThat(adresseResource.poststed).isEqualTo("testPostSted")
    }

    @Test
    fun toAdresseResourceNullFields() {
        val adresseResource = adresseMappingService.toAdresseResource(AdresseDto.builder().build())

        assertThat(adresseResource.adresselinje).isNull()
        assertThat(adresseResource.postnummer).isNull()
        assertThat(adresseResource.poststed).isNull()
    }
}
