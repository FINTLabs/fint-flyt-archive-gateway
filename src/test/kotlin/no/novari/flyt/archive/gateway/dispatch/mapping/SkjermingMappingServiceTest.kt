package no.novari.flyt.archive.gateway.dispatch.mapping

import no.novari.flyt.archive.gateway.dispatch.model.instance.SkjermingDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SkjermingMappingServiceTest {
    private lateinit var skjermingMappingService: SkjermingMappingService

    @BeforeEach
    fun setUp() {
        skjermingMappingService = SkjermingMappingService()
    }

    @Test
    fun `given a null dto, returns null`() {
        assertThat(skjermingMappingService.toSkjermingResource(null)).isNull()
    }

    @Test
    fun `given a valid dto, maps to SkjermingResource`() {
        val skjermingDto =
            SkjermingDto
                .builder()
                .tilgangsrestriksjon("Tilgangsrestriksjon")
                .skjermingshjemmel("Skjermingshjemmel")
                .build()

        val result = skjermingMappingService.toSkjermingResource(skjermingDto)

        assertThat(result).isNotNull
        assertThat(result!!.tilgangsrestriksjon.first().href).isEqualTo("Tilgangsrestriksjon")
        assertThat(result.skjermingshjemmel.first().href).isEqualTo("Skjermingshjemmel")
    }
}
