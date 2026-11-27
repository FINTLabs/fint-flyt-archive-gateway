package no.novari.flyt.archive.gateway

import com.fasterxml.jackson.annotation.JsonInclude
import no.novari.fint.model.arkiv.noark.Avskrivning
import no.novari.fint.model.resource.arkiv.noark.JournalpostResource
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.util.StdConverter
import java.time.temporal.ChronoUnit
import java.util.Date

@Configuration
class FintArchiveJacksonConfiguration {
    @Bean
    fun avskrivningSerialization(): JsonMapperBuilderCustomizer =
        JsonMapperBuilderCustomizer { builder ->
            // The archive RestClient uses Spring's shared mapper; keep date formatting serialization-only.
            builder.addMixIn(JournalpostResource::class.java, JournalpostResourceMixin::class.java)
            builder.addMixIn(Avskrivning::class.java, AvskrivningMixin::class.java)
        }
}

private abstract class JournalpostResourceMixin {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    abstract fun getAvskrivning(): Avskrivning?
}

@JsonInclude(JsonInclude.Include.NON_NULL)
private abstract class AvskrivningMixin {
    @JsonSerialize(converter = AvskrivningsdatoConverter::class)
    abstract fun getAvskrivningsdato(): Date?
}

private class AvskrivningsdatoConverter : StdConverter<Date, String>() {
    override fun convert(value: Date): String = value.toInstant().truncatedTo(ChronoUnit.SECONDS).toString()
}
