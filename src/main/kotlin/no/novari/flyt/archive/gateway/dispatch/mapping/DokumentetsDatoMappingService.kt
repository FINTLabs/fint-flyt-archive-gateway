package no.novari.flyt.archive.gateway.dispatch.mapping

import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date

@Service
class DokumentetsDatoMappingService {
    fun toDokumentetsDatoDateOrNull(dokumentetsDato: String?): Date? =
        dokumentetsDato
            ?.trim()
            ?.takeIf(String::isNotEmpty)
            ?.let(::toDokumentetsDatoDate)

    fun toDokumentetsDatoDate(dokumentetsDato: String): Date {
        val trimmedDokumentetsDato = dokumentetsDato.trim()

        return parseDate(trimmedDokumentetsDato)
            ?: parseDateTime(trimmedDokumentetsDato)
            ?: throw InvalidDokumentetsDatoException(dokumentetsDato)
    }

    private fun parseDate(dokumentetsDato: String): Date? =
        runCatching { LocalDate.parse(dokumentetsDato, DateTimeFormatter.ISO_LOCAL_DATE) }
            .getOrNull()
            ?.atTime(LocalTime.NOON)
            ?.atOffset(ZoneOffset.UTC)
            ?.toInstant()
            ?.truncatedTo(ChronoUnit.SECONDS)
            ?.let(Date::from)

    private fun parseDateTime(dokumentetsDato: String): Date? =
        runCatching { OffsetDateTime.parse(dokumentetsDato, DateTimeFormatter.ISO_OFFSET_DATE_TIME) }
            .getOrNull()
            ?.toInstant()
            ?.truncatedTo(ChronoUnit.SECONDS)
            ?.let(Date::from)
}
