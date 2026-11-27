package no.novari.flyt.archive.gateway.kafka.error

import no.novari.flyt.archive.gateway.kafka.KafkaTopicProperties
import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeaders
import no.novari.flyt.kafka.instanceflow.producing.send
import no.novari.flyt.kafka.model.Error
import no.novari.flyt.kafka.model.ErrorCollection
import no.novari.flyt.kafka.model.InstanceErrorEvent
import no.novari.flyt.kafka.model.InstanceErrorOrigin
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
class InstanceErrorTopicConfiguration {
    @Bean
    fun instanceErrorTopic(topics: KafkaTopics): EventTopicName = topics.errorEvent("instance-error")

    @Bean
    fun instanceErrorTopicCreation(
        topics: KafkaTopics,
        instanceErrorTopic: EventTopicName,
        kafkaTopicProperties: KafkaTopicProperties,
    ): NewTopic =
        topics.newTopic(
            instanceErrorTopic,
            EventTopicConfig(
                retention = requireNotNull(kafkaTopicProperties.instanceProcessingEventsRetentionTime),
                cleanupFrequency = EventCleanupFrequency.NORMAL,
                partitions = 1,
            ),
        )
}

@Service
class InstanceErrorEventProducerService(
    private val kafkaTemplate: KafkaTemplate<String, InstanceErrorEvent>,
    // By name: there are several EventTopicName beans.
    @param:Qualifier("instanceErrorTopic")
    private val instanceErrorTopic: EventTopicName,
) {
    fun publishInstanceDispatchDeclinedErrorEvent(
        instanceFlowHeaders: InstanceFlowHeaders,
        errorMessage: String?,
    ) {
        publish(instanceFlowHeaders, ErrorCode.INSTANCE_DISPATCH_DECLINED_ERROR, errorMessage)
    }

    fun publishGeneralSystemErrorEvent(
        instanceFlowHeaders: InstanceFlowHeaders,
        errorMessage: String?,
    ) {
        publish(
            instanceFlowHeaders,
            ErrorCode.GENERAL_SYSTEM_ERROR,
            if (!errorMessage.isNullOrEmpty()) errorMessage else "Unknown error occurred",
        )
    }

    private fun publish(
        instanceFlowHeaders: InstanceFlowHeaders,
        errorCode: ErrorCode,
        errorMessage: String?,
    ) {
        kafkaTemplate.send(
            instanceErrorTopic,
            instanceFlowHeaders,
            InstanceErrorEvent(
                name = InstanceErrorOrigin.DISPATCHING,
                errors =
                    ErrorCollection(
                        Error(errorCode = errorCode.getCode(), args = mapOf("errorMessage" to errorMessage)),
                    ),
            ),
        )
    }
}
