package no.novari.flyt.archive.gateway.kafka;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "novari.flyt.archive.gateway.kafka.topic")
public class KafkaTopicProperties {

    private Duration instanceProcessingEventsRetentionTime;
}
