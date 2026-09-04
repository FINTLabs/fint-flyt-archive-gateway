package no.novari.flyt.archive.gateway.dispatch.mapping

import org.springframework.stereotype.Service
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
        val parsedDokumentetsDato =
            runCatching { LocalDateTime.parse(dokumentetsDato, FORMATTER) }
                .getOrElse { throw InvalidDokumentetsDatoException(dokumentetsDato) }

        return FORMATTER.format(parsedDokumentetsDato)
    }

    companion object {
        private val FORMATTER: DateTimeFormatter =
            DateTimeFormatterBuilder()
                .appendPattern("uuuu-MM-dd'T'HH:mm:ss'Z'")
                .toFormatter()
                .withResolverStyle(ResolverStyle.STRICT)
    }
}
