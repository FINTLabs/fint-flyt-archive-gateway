package no.fintlabs.flyt.gateway.application.archive.resource.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "fint.flyt.gateway.application.archive.resource.publishing.pull")
public class ResourcePublishingPullConfigurationProperties {
    private Duration initialDelay;
    private Duration fixedDelay;
}
