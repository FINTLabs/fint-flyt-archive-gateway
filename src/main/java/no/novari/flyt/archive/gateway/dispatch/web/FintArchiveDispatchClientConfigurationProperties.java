package no.novari.flyt.archive.gateway.dispatch.web;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties("novari.flyt.archive.gateway.dispatch.fint-client")
public class FintArchiveDispatchClientConfigurationProperties {
    private Duration postFileTimeout;
    private Duration postCaseTimeout;
    private Duration postRecordTimeout;
    private Duration getStatusTimeout;
    private Duration createdLocationPollTotalTimeout;
    private Duration createdLocationPollBackoffMinDelay;
    private Duration createdLocationPollBackoffMaxDelay;
}
