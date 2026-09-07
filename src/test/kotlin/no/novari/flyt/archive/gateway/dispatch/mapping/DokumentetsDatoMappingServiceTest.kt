package no.novari.flyt.archive.gateway.dispatch.mapping

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.sql.Date

class DokumentetsDatoMappingServiceTest {
    private val dokumentetsDatoMappingService = DokumentetsDatoMappingService()

    @Test
    fun `given date, returns sql date with same date`() {
        val result = dokumentetsDatoMappingService.toDateOrNull("2026-08-24")

        assertThat(result).isEqualTo(Date.valueOf("2026-08-24"))
    }

    @Test
    fun `given blank dokumentetsDato, returns null`() {
        val result = dokumentetsDatoMappingService.toDateOrNull(" ")

        assertThat(result).isNull()
    }

    @Test
    fun `given missing dokumentetsDato, returns null`() {
        val result = dokumentetsDatoMappingService.toDateOrNull(null)

        assertThat(result).isNull()
    }

    @Test
    fun `given date time, throws invalid date exception`() {
        assertThatThrownBy {
            dokumentetsDatoMappingService.toDateOrNull("2026-08-24T09:12:48Z")
        }.isInstanceOf(InvalidDokumentetsDatoException::class.java)
            .hasMessage(
                "Ugyldig dokumentetsDato='2026-08-24T09:12:48Z'. Feltet må være på formatet " +
                    "YYYY-MM-DD. Korriger verdien og send instansen på nytt.",
            )
    }

    @Test
    fun `given date with surrounding whitespace, throws invalid date exception`() {
        assertThatThrownBy {
            dokumentetsDatoMappingService.toDateOrNull(" 2026-08-24 ")
        }.isInstanceOf(InvalidDokumentetsDatoException::class.java)
            .hasMessage(
                "Ugyldig dokumentetsDato=' 2026-08-24 '. Feltet må være på formatet " +
                    "YYYY-MM-DD. Korriger verdien og send instansen på nytt.",
            )
    }

    @Test
    fun `given invalid calendar date, throws invalid date exception`() {
        assertThatThrownBy {
            dokumentetsDatoMappingService.toDateOrNull("2026-02-30")
        }.isInstanceOf(InvalidDokumentetsDatoException::class.java)
            .hasMessage(
                "Ugyldig dokumentetsDato='2026-02-30'. Feltet må være på formatet " +
                    "YYYY-MM-DD. Korriger verdien og send instansen på nytt.",
            )
    }
}
