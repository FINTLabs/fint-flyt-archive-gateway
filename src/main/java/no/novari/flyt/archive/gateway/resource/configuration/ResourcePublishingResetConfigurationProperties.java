package no.novari.flyt.archive.gateway.resource.configuration;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Duration;
import java.time.LocalTime;

@Setter
@Getter
@ConfigurationProperties(prefix = "novari.flyt.archive.gateway.resource.publishing.reset")
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
