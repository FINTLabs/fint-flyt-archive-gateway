package no.fintlabs.flyt.gateway.application.archive.resource.web;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties("fint.flyt.gateway.application.archive.resource.fint-client")
public class FintArchiveResourceClientProperties {
    private Long getResourcesLastUpdatedTimeoutMillis;
    private Long findCasesWithFilterTimeoutMillis;
    private Long findCasesWithFilterMaxAttempts;
    private Long getResourceTimeoutMillis;
}
