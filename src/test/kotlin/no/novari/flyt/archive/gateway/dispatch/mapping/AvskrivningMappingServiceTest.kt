package no.novari.flyt.archive.gateway.dispatch.mapping

import no.novari.cache.FintCache
import no.novari.fint.model.resource.arkiv.kodeverk.JournalpostTypeResource
import no.novari.flyt.archive.gateway.dispatch.model.instance.AvskrivningDto
import no.novari.flyt.archive.gateway.dispatch.model.instance.JournalpostDto
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.NullAndEmptySource
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.Instant
import java.time.temporal.ChronoUnit

class AvskrivningMappingServiceTest {
    private val journalpostTypeResourceCache: FintCache<String, JournalpostTypeResource> = mock()
    private val service = AvskrivningMappingService(journalpostTypeResourceCache)
    private val incomingType = "https://example.com/arkiv/kodeverk/journalposttype/systemid/42"

    private fun incomingJournalpost(avskrivning: AvskrivningDto): JournalpostDto {
        whenever(journalpostTypeResourceCache.get(incomingType))
            .thenReturn(JournalpostTypeResource().apply { kode = "I" })
        return JournalpostDto(journalposttype = incomingType, avskrivning = avskrivning)
    }

    @Test
    fun `uses journalpost type code from cache and maps date and method without avskrevetAv`() {
        val dto = incomingJournalpost(AvskrivningDto("2026-08-24T09:12:48Z", "TE"))

        val result = service.toAvskrivning(dto)!!

        assertThat(result.avskrivningsmate).isEqualTo("TE")
        assertThat(result.avskrivningsdato.toInstant()).isEqualTo(Instant.parse("2026-08-24T09:12:48Z"))
        assertThat(result.avskrevetAv).isNull()
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = [" "])
    fun `defaults missing or blank date to current time at dispatch`(date: String?) {
        val dto = incomingJournalpost(AvskrivningDto(date, "TE"))
        val before = Instant.now().truncatedTo(ChronoUnit.SECONDS)

        val result = service.toAvskrivning(dto)!!

        assertThat(result.avskrivningsdato.toInstant())
            .isBetween(before, Instant.now())
        assertThat(result.avskrivningsmate).isEqualTo("TE")
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = [" "])
    fun `omits avskrivning when method is missing or blank even with a date`(method: String?) {
        val dto = incomingJournalpost(AvskrivningDto("invalid date", method))

        assertThat(service.toAvskrivning(dto)).isNull()
    }

    @Test
    fun `omits absent avskrivning`() {
        assertThat(service.toAvskrivning(JournalpostDto(journalposttype = incomingType))).isNull()
    }

    @ParameterizedTest
    @ValueSource(strings = ["U", "N", "X", "i", ""])
    fun `omits entire avskrivning for other journalpost types regardless of date or link suffix`(code: String) {
        val type = "https://example.com/arkiv/kodeverk/journalposttype/systemid/I"
        whenever(journalpostTypeResourceCache.get(type)).thenReturn(JournalpostTypeResource().apply { kode = code })
        val dto = JournalpostDto(journalposttype = type, avskrivning = AvskrivningDto("invalid date", "TE"))

        assertThat(service.toAvskrivning(dto)).isNull()
    }

    @Test
    fun `omits avskrivning when journalpost type is missing`() {
        val dto = JournalpostDto(avskrivning = AvskrivningDto("invalid date", "TE"))

        assertThat(service.toAvskrivning(dto)).isNull()
    }

    @Test
    fun `fails when journalpost type is missing from cache instead of silently discarding avskrivning`() {
        val dto = JournalpostDto(journalposttype = incomingType, avskrivning = AvskrivningDto(avskrivningsmate = "TE"))

        assertThatThrownBy { service.toAvskrivning(dto) }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("Fant ikke kode for journalposttype")
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "not a date",
            "2026-08-24",
            "2026-02-30T09:12:48Z",
            "2026-08-24T24:00:00Z",
            "2026-08-24T09:12:48",
            "2026-08-24T09:12Z",
            "2026-08-24T09:12:48.123Z",
            "2026-08-24T09:12:48+02:00",
            " 2026-08-24T09:12:48Z ",
        ],
    )
    fun `rejects invalid avskrivningsdato for incoming journalpost`(date: String) {
        val dto = incomingJournalpost(AvskrivningDto(date, "TE"))

        assertThatThrownBy { service.toAvskrivning(dto) }
            .isInstanceOf(InvalidAvskrivningsdatoException::class.java)
            .hasMessageContaining("YYYY-MM-DDThh:mm:ssZ (UTC)")
    }
}
