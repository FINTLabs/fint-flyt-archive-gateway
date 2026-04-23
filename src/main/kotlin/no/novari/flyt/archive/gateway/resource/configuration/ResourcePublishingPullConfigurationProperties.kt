package no.novari.flyt.archive.gateway.resource.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "novari.flyt.archive.gateway.resource.publishing.pull")
class ResourcePublishingPullConfigurationProperties {
    var initialDelay: Duration? = null
    var fixedDelay: Duration? = null
}
