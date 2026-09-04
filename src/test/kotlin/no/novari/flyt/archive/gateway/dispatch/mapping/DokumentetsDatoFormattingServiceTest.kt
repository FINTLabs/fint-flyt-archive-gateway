package no.novari.flyt.archive.gateway.dispatch.mapping

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class DokumentetsDatoFormattingServiceTest {
    private val dokumentetsDatoFormattingService = DokumentetsDatoFormattingService()

    @Test
    fun `given ISO 8601 date time, returns same value without changing clock time`() {
        val result = dokumentetsDatoFormattingService.validateAndFormatOrNull("2026-08-24T09:12:48Z")

        assertThat(result).isEqualTo("2026-08-24T09:12:48Z")
    }

    @Test
    fun `given date time with surrounding whitespace, returns trimmed ISO 8601 date time`() {
        val result = dokumentetsDatoFormattingService.validateAndFormatOrNull(" 2026-08-24T09:12:48Z ")

        assertThat(result).isEqualTo("2026-08-24T09:12:48Z")
    }

    @Test
    fun `given blank dokumentetsDato, returns null`() {
        val result = dokumentetsDatoFormattingService.validateAndFormatOrNull(" ")

        assertThat(result).isNull()
    }

    @Test
    fun `given date without time, throws invalid date exception`() {
        assertThatThrownBy {
            dokumentetsDatoFormattingService.validateAndFormatOrNull("2026-08-24")
        }.isInstanceOf(InvalidDokumentetsDatoException::class.java)
            .hasMessage(
                "Ugyldig dokumentetsDato='2026-08-24'. Feltet må være på ISO 8601-format " +
                    "YYYY-MM-DDThh:mm:ssZ. Korriger verdien og send instansen på nytt.",
            )
    }

    @Test
    fun `given invalid calendar date, throws invalid date exception`() {
        assertThatThrownBy {
            dokumentetsDatoFormattingService.validateAndFormatOrNull("2026-02-30T09:12:48Z")
        }.isInstanceOf(InvalidDokumentetsDatoException::class.java)
            .hasMessage(
                "Ugyldig dokumentetsDato='2026-02-30T09:12:48Z'. Feltet må være på ISO 8601-format " +
                    "YYYY-MM-DDThh:mm:ssZ. Korriger verdien og send instansen på nytt.",
            )
    }
}
