package no.novari.flyt.archive.gateway

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.util.StdConverter
import no.novari.fint.model.arkiv.noark.Avskrivning
import no.novari.fint.model.resource.arkiv.noark.JournalpostResource
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.temporal.ChronoUnit
import java.util.Date

@Configuration
class FintArchiveJacksonConfiguration {
    @Bean
    fun avskrivningSerialization(): Jackson2ObjectMapperBuilderCustomizer =
        Jackson2ObjectMapperBuilderCustomizer { builder ->
            builder.mixIn(JournalpostResource::class.java, JournalpostResourceMixin::class.java)
            builder.mixIn(Avskrivning::class.java, AvskrivningMixin::class.java)
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
