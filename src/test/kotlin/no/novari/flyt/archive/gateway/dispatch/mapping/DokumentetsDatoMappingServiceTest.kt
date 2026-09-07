package no.novari.flyt.archive.gateway.dispatch.mapping

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import no.novari.fint.model.resource.arkiv.noark.JournalpostResource
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.Instant

class DokumentetsDatoMappingServiceTest {
    private val dokumentetsDatoMappingService = DokumentetsDatoMappingService()

    @Test
    fun `given date, returns date at noon utc`() {
        val result = dokumentetsDatoMappingService.toDateOrNull("2026-08-24")

        assertThat(result?.toInstant()).isEqualTo(Instant.parse("2026-08-24T12:00:00Z"))
    }

    @Test
    fun `given date, serializes dokumentetsDato as noon utc date time`() {
        val journalpostResource =
            JournalpostResource().apply {
                dokumentetsDato = dokumentetsDatoMappingService.toDate("2026-08-24")
            }

        val json =
            ObjectMapper()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .writeValueAsString(journalpostResource)

        assertThat(json).contains("\"dokumentetsDato\":\"2026-08-24T12:00:00.000+00:00\"")
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
