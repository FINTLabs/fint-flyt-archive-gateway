package no.novari.flyt.archive.gateway.dispatch.mapping

import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Date

@Service
class DokumentetsDatoMappingService {
    fun toDate(value: String): Date {
        val trimmedValue = value.trim()
        if (trimmedValue.isEmpty()) {
            throw InvalidDokumentetsDatoException(value)
        }

        val localDate = parseLocalDate(trimmedValue) ?: throw InvalidDokumentetsDatoException(value)
        return Date.from(localDate.atTime(LocalTime.NOON).toInstant(ZoneOffset.UTC))
    }

    private fun parseLocalDate(value: String): LocalDate? =
        dateFormatters.firstNotNullOfOrNull { formatter ->
            runCatching { LocalDate.parse(value, formatter) }.getOrNull()
        } ?: localDateTimeFormatters.firstNotNullOfOrNull { formatter ->
            runCatching { LocalDateTime.parse(value, formatter).toLocalDate() }.getOrNull()
        } ?: offsetDateTimeFormatters.firstNotNullOfOrNull { formatter ->
            runCatching { OffsetDateTime.parse(value, formatter).toLocalDate() }.getOrNull()
        }

    companion object {
        private val dateFormatters =
            listOf(
                DateTimeFormatter.ISO_LOCAL_DATE,
                DateTimeFormatter.ofPattern("M/d/yyyy"),
            )
        private val localDateTimeFormatters =
            listOf(
                DateTimeFormatter.ISO_LOCAL_DATE_TIME,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("M/d/yyyy HH:mm:ss"),
            )
        private val offsetDateTimeFormatters = listOf(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    }
}
