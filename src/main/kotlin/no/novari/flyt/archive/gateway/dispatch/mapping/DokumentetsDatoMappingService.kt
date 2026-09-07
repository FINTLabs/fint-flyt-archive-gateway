package no.novari.flyt.archive.gateway.dispatch.mapping

import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.ResolverStyle
import java.util.Date

@Service
class DokumentetsDatoMappingService {
    fun toDateOrNull(dokumentetsDato: String?): Date? {
        if (dokumentetsDato.isNullOrBlank()) {
            return null
        }
        return toDate(dokumentetsDato)
    }

    fun toDate(dokumentetsDato: String): Date {
        if (!DATE_PATTERN.matches(dokumentetsDato)) {
            throw InvalidDokumentetsDatoException(dokumentetsDato)
        }

        val parsedDate =
            runCatching {
                LocalDate.parse(dokumentetsDato, FORMATTER)
            }.getOrElse {
                throw InvalidDokumentetsDatoException(dokumentetsDato)
            }

        return Date.from(parsedDate.atTime(LocalTime.NOON).toInstant(ZoneOffset.UTC))
    }

    companion object {
        private val DATE_PATTERN = Regex("\\d{4}-\\d{2}-\\d{2}")

        private val FORMATTER: DateTimeFormatter =
            DateTimeFormatterBuilder()
                .appendPattern("uuuu-MM-dd")
                .toFormatter()
                .withResolverStyle(ResolverStyle.STRICT)
    }
}
