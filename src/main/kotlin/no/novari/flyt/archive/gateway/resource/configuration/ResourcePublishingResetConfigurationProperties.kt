package no.novari.flyt.archive.gateway.resource.configuration

import jakarta.validation.constraints.NotNull
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.format.annotation.DateTimeFormat
import java.time.Duration
import java.time.LocalTime

@ConfigurationProperties(prefix = "novari.flyt.archive.gateway.resource.publishing.reset")
class ResourcePublishingResetConfigurationProperties {
    @field:NotNull
    @field:DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    var fromTimeOfDay: LocalTime? = null

    @field:NotNull
    @field:DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    var toTimeOfDay: LocalTime? = null

    @field:NotNull
    var resourceTopicRetentionTimeOffset: Duration? = null
}
