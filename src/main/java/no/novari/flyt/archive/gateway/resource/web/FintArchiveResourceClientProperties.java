package no.novari.flyt.archive.gateway.resource.web;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties("novari.flyt.archive.gateway.resource.fint-client")
public class FintArchiveResourceClientProperties {
    private Duration getResourcesLastUpdatedTimeout;
    private Duration findCasesWithFilterTimeout;
    private Long findCasesWithFilterMaxAttempts;
    private Duration findCasesWithFilterBackoffRetryMinDelay;
    private Duration findCasesWithFilterBackoffRetryMaxDelay;
    private Duration getResourceTimeout;
}
