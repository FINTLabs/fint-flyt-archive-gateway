package no.novari.flyt.archive.gateway.resource.configuration;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@Setter
@Getter
@ConfigurationProperties(prefix = "novari.flyt.archive.gateway.resource.publishing")
public class ResourcePublishingConfigurationProperties {

    @Valid
    private ResourcePublishingResetConfigurationProperties reset;

    @Valid
    private ResourcePublishingPullConfigurationProperties pull;
}
