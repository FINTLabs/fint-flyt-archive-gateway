package no.novari.flyt.archive.gateway

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import java.text.SimpleDateFormat
import java.util.TimeZone

object FintArchiveObjectMapperFactory {
    fun create(baseObjectMapper: ObjectMapper): ObjectMapper =
        baseObjectMapper
            .copy()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .setDateFormat(fintDateTimeFormat())

    private fun fintDateTimeFormat(): SimpleDateFormat =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX").apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
}
