package no.fintlabs.flyt.gateway.application.archive.resource.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
public class RefreshConfiguration {
    private long intervalMs;
    private long topicRetentionTimeOffsetMs;

    public long getTopicRetentionTimeMs() {
        return intervalMs + topicRetentionTimeOffsetMs;
    }
}
