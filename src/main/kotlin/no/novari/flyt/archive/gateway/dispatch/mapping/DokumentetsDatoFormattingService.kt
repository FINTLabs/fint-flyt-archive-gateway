package no.novari.flyt.archive.gateway.dispatch.mapping

import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.ResolverStyle

@Service
class DokumentetsDatoFormattingService {
    fun validateAndFormatOrNull(dokumentetsDato: String?): String? {
        val trimmedDokumentetsDato = dokumentetsDato?.trim()?.takeIf(String::isNotEmpty) ?: return null
        return validateAndFormat(trimmedDokumentetsDato)
    }

    fun validateAndFormat(dokumentetsDato: String): String {
        val parsedDateTime =
            runCatching {
                LocalDateTime.parse(dokumentetsDato, DATE_TIME_FORMATTER)
            }.getOrElse {
                runCatching {
                    LocalDate.parse(dokumentetsDato, DATE_FORMATTER).atStartOfDay()
                }.getOrElse {
                    throw InvalidDokumentetsDatoException(dokumentetsDato)
                }
            }

        return DATE_TIME_FORMATTER.format(parsedDateTime)
    }

    companion object {
        private val DATE_TIME_FORMATTER: DateTimeFormatter =
            DateTimeFormatterBuilder()
                .appendPattern("uuuu-MM-dd'T'HH:mm:ss'Z'")
                .toFormatter()
                .withResolverStyle(ResolverStyle.STRICT)

        private val DATE_FORMATTER: DateTimeFormatter =
            DateTimeFormatterBuilder()
                .appendPattern("uuuu-MM-dd")
                .toFormatter()
                .withResolverStyle(ResolverStyle.STRICT)
    }
}
