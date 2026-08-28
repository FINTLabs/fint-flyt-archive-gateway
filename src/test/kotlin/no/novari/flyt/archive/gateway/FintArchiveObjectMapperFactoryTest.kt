package no.novari.flyt.archive.gateway

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.novari.fint.model.resource.arkiv.noark.JournalpostResource
import no.novari.flyt.archive.gateway.dispatch.model.JournalpostWrapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.util.Date

class FintArchiveObjectMapperFactoryTest {
    @Test
    fun `serializes journalpost dokumentetsDato as FINT ISO 8601 dateTime without milliseconds`() {
        val objectMapper = FintArchiveObjectMapperFactory.create(jacksonObjectMapper())
        val journalpostResource =
            JournalpostResource().apply {
                dokumentetsDato = noonUtcDate(2026, 8, 24)
            }

        val json = objectMapper.writeValueAsString(JournalpostWrapper(journalpostResource))

        assertThat(json).contains("\"dokumentetsDato\":\"2026-08-24T12:00:00Z\"")
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
