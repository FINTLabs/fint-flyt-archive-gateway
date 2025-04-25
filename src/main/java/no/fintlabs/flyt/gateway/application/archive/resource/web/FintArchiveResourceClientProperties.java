package no.fintlabs.flyt.gateway.application.archive.resource.web;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties("fint.flyt.gateway.application.archive.resource.fint-client")
public class FintArchiveResourceClientProperties {
    private Long getResourcesLastUpdatedTimeoutMillis;
    private Long findCasesWithFilterTimeoutMillis;
    private Long findCasesWithFilterMaxAttempts;
    private Long getResourceTimeoutMillis;
}
