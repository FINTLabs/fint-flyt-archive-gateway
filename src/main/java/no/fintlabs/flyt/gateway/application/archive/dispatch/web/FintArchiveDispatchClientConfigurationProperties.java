package no.fintlabs.flyt.gateway.application.archive.dispatch.web;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties("fint.flyt.gateway.application.archive.dispatch.fint-client")
public class FintArchiveDispatchClientConfigurationProperties {
    private Long postFileTimeoutMillis;
    private Long postCaseTimeoutMillis;
    private Long postRecordTimeoutMillis;
    private Long getStatusTimeoutMillis;
    private Long createdLocationPollTotalTimeoutMillis;
    private Long createdLocationPollBackoffMinDelayMillis;
    private Long createdLocationPollBackoffMaxDelayMillis;
}
