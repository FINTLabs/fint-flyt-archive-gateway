package no.fintlabs.flyt.gateway.application.archive.resource.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.time.LocalTime;

@Setter
@Getter
@ConfigurationProperties(prefix = "fint.flyt.gateway.application.archive.resource.publishing.reset")
public class ResourcePublishingResetConfigurationProperties {

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime fromTimeOfDay;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime toTimeOfDay;

    @NotNull
    private Duration resourceTopicRetentionTimeOffset;
}
