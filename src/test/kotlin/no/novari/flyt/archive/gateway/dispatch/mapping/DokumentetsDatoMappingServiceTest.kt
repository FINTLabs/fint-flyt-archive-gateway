package no.novari.flyt.archive.gateway.dispatch.mapping

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.Date

class DokumentetsDatoMappingServiceTest {
    private val dokumentetsDatoMappingService = DokumentetsDatoMappingService()

    @Test
    fun `given ISO local date, maps to noon UTC`() {
        val result = dokumentetsDatoMappingService.toDokumentetsDatoDateOrNull("1990-03-29")

        assertThat(result).isEqualTo(Date.from(Instant.parse("1990-03-29T12:00:00Z")))
    }

    @Test
    fun `given ISO offset date time, maps to UTC instant`() {
        val result = dokumentetsDatoMappingService.toDokumentetsDatoDateOrNull("1990-03-29T14:30:00+02:00")

        assertThat(result).isEqualTo(Date.from(Instant.parse("1990-03-29T12:30:00Z")))
    }

    @Test
    fun `given blank dokumentetsDato, returns null`() {
        val result = dokumentetsDatoMappingService.toDokumentetsDatoDateOrNull(" ")

        assertThat(result).isNull()
    }

    @Test
    fun `given invalid dokumentetsDato, throws invalid date exception with field message`() {
        assertThatThrownBy {
            dokumentetsDatoMappingService.toDokumentetsDatoDateOrNull("1990-02-30")
        }.isInstanceOf(InvalidDokumentetsDatoException::class.java)
            .hasMessage(
                "Invalid dokumentetsDato='1990-02-30'. Expected ISO 8601 date " +
                    "'yyyy-MM-dd' or date-time 'yyyy-MM-dd'T'HH:mm:ssZ'.",
            )
    }
}
