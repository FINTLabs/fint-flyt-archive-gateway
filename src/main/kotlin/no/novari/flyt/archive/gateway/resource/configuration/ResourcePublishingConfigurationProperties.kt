package no.novari.flyt.archive.gateway.resource.configuration

import jakarta.validation.Valid
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties(prefix = "novari.flyt.archive.gateway.resource.publishing")
class ResourcePublishingConfigurationProperties {
    @field:Valid
    var reset: ResourcePublishingResetConfigurationProperties? = null

    @field:Valid
    var pull: ResourcePublishingPullConfigurationProperties? = null
}
