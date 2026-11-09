package no.novari.flyt.archive.gateway

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
import no.novari.fint.model.arkiv.noark.Avskrivning
import no.novari.fint.model.resource.arkiv.noark.JournalpostResource
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    abstract fun getAvskrivningsdato(): Date?
}
