package no.novari.flyt.archive.gateway.kafka

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import java.time.Duration

@Component
@ConfigurationProperties(prefix = "novari.flyt.archive.gateway.kafka.topic")
class KafkaTopicProperties {
    var instanceProcessingEventsRetentionTime: Duration? = null
}
