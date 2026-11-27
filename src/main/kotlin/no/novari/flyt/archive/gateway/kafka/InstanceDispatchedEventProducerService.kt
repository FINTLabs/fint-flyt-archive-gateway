package no.novari.flyt.archive.gateway.kafka

import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeaders
import no.novari.flyt.kafka.instanceflow.producing.send
import no.novari.kafka.starter.topic.EventCleanupFrequency
import no.novari.kafka.starter.topic.EventTopicConfig
import no.novari.kafka.starter.topic.EventTopicName
import no.novari.kafka.starter.topic.KafkaTopics
import org.apache.kafka.clients.admin.NewTopic
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Configuration
class InstanceDispatchedTopicConfiguration {
    @Bean
    fun instanceDispatchedTopic(topics: KafkaTopics): EventTopicName = topics.event("instance-dispatched")

    @Bean
    fun instanceDispatchedTopicCreation(
        topics: KafkaTopics,
        instanceDispatchedTopic: EventTopicName,
        kafkaTopicProperties: KafkaTopicProperties,
    ): NewTopic =
        topics.newTopic(
            instanceDispatchedTopic,
            EventTopicConfig(
                retention = requireNotNull(kafkaTopicProperties.instanceProcessingEventsRetentionTime),
                cleanupFrequency = EventCleanupFrequency.NORMAL,
                partitions = 1,
            ),
        )
}

@Service
class InstanceDispatchedEventProducerService(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    // By name: there are several EventTopicName beans.
    @param:Qualifier("instanceDispatchedTopic")
    private val instanceDispatchedTopic: EventTopicName,
) {
    fun publish(instanceFlowHeaders: InstanceFlowHeaders) {
        kafkaTemplate.send(instanceDispatchedTopic, instanceFlowHeaders, null)
    }
}
