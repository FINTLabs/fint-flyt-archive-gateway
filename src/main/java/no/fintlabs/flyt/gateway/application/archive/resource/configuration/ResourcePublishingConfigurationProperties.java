package no.fintlabs.flyt.gateway.application.archive.resource.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;

@Validated
@Setter
@Getter
@ConfigurationProperties(prefix = "fint.flyt.gateway.application.archive.resource.publishing")
public class ResourcePublishingConfigurationProperties {

    @Valid
    private ResourcePublishingResetConfigurationProperties reset;

    @Valid
    private ResourcePublishingPullConfigurationProperties pull;
}
