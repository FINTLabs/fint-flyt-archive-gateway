package no.fintlabs.flyt.gateway.application.archive.resource.configuration;

import lombok.Getter;
import lombok.Setter;
import no.fintlabs.flyt.gateway.application.archive.resource.configuration.validation.FromTimeOfDayBeforeOrSameAsToTimeOfDay;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.time.LocalTime;

@Validated
@FromTimeOfDayBeforeOrSameAsToTimeOfDay
@Setter
@Getter
@ConfigurationProperties(prefix = "fint.flyt.gateway.application.archive.resource.publishing.refresh")
public class ResourcePublishingRefreshConfigurationProperties {

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime fromTimeOfDay;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime toTimeOfDay;

    @NotNull
    private Duration resourceTopicRetentionTimeOffset;
}
