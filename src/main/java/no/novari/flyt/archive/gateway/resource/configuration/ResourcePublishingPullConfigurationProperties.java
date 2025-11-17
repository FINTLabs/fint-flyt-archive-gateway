package no.novari.flyt.archive.gateway.resource.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "novari.flyt.archive.gateway.resource.publishing.pull")
public class ResourcePublishingPullConfigurationProperties {
    private Duration initialDelay;
    private Duration fixedDelay;
}
