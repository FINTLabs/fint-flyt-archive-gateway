package no.fintlabs.flyt.gateway.application.archive.kafka;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "fint.flyt.gateway.application.archive.kafka.topic")
public class KafkaTopicProperties {

    private long instanceProcessingEventsRetentionTimeMs;
}
