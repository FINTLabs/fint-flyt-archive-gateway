package no.novari.flyt.archive.gateway.dispatch.mapping

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.util.Date

class DokumentetsDatoMappingServiceTest {
    private val dokumentetsDatoMappingService = DokumentetsDatoMappingService()

    @Test
    fun `maps iso date to noon utc dateTime`() {
        val result = dokumentetsDatoMappingService.toDate("2026-08-24")

        assertThat(result).isEqualTo(noonUtcDate(2026, 8, 24))
    }

    @Test
    fun `maps iso dateTime to noon utc dateTime for the same date`() {
        val result = dokumentetsDatoMappingService.toDate("2026-08-24T09:12:48Z")

        assertThat(result).isEqualTo(noonUtcDate(2026, 8, 24))
    }

    @Test
    fun `maps mapped instance dateTime to noon utc dateTime`() {
        val result = dokumentetsDatoMappingService.toDate("08/24/2026 09:12:48")

        assertThat(result).isEqualTo(noonUtcDate(2026, 8, 24))
    }

    @Test
    fun `throws exception for invalid dokumentetsDato`() {
        assertThatThrownBy { dokumentetsDatoMappingService.toDate("not a date") }
            .isInstanceOf(InvalidDokumentetsDatoException::class.java)
            .hasMessageContaining("Invalid dokumentetsDato 'not a date'")
    }

    private fun noonUtcDate(
        year: Int,
        month: Int,
        dayOfMonth: Int,
    ): Date =
        Date.from(
            LocalDate
                .of(year, month, dayOfMonth)
                .atTime(LocalTime.NOON)
                .toInstant(ZoneOffset.UTC),
        )
}
