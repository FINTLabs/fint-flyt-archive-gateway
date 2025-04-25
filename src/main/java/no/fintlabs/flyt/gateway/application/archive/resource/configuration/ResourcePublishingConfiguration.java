package no.fintlabs.flyt.gateway.application.archive.resource.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties("fint.flyt.gateway.application.archive.resource.publishing")
public class ResourcePublishingConfiguration {
    private RefreshConfiguration refresh;
    private PullConfiguration pull;
}
