package no.fintlabs.flyt.gateway.application.archive.resource.configuration;

import lombok.Data;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class RefreshConfiguration {
    private long intervalMs;
    private long topicRetentionTimeOffsetMs;

    public long getTopicRetentionTimeMs() {
        return intervalMs + topicRetentionTimeOffsetMs;
    }
}
