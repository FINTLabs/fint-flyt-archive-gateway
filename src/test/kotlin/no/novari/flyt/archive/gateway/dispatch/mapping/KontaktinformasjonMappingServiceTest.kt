package no.novari.flyt.archive.gateway.dispatch.mapping

import no.novari.flyt.archive.gateway.dispatch.model.instance.KontaktinformasjonDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class KontaktinformasjonMappingServiceTest {
    private lateinit var kontaktinformasjonMappingService: KontaktinformasjonMappingService

    @BeforeEach
    fun setup() {
        kontaktinformasjonMappingService = KontaktinformasjonMappingService()
    }

    @Test
    fun testToKontaktinformasjonWithNullInput() {
        assertThat(kontaktinformasjonMappingService.toKontaktinformasjon(null)).isNull()
    }

    @Test
    fun testToKontaktinformasjonWithValidInput() {
        val dto =
            KontaktinformasjonDto
                .builder()
                .epostadresse("testEmail@test.com")
                .telefonnummer("123456789")
                .mobiltelefonnummer("987654321")
                .build()

        val actualKontaktinformasjon = kontaktinformasjonMappingService.toKontaktinformasjon(dto)

        assertThat(actualKontaktinformasjon!!.epostadresse).isEqualTo("testEmail@test.com")
        assertThat(actualKontaktinformasjon.telefonnummer).isEqualTo("123456789")
        assertThat(actualKontaktinformasjon.mobiltelefonnummer).isEqualTo("987654321")
    }
}
