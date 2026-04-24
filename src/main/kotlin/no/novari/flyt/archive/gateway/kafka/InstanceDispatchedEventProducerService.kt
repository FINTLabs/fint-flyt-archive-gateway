package no.novari.flyt.archive.gateway.kafka

import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeaders
import no.novari.flyt.kafka.instanceflow.producing.InstanceFlowProducerRecord
import no.novari.flyt.kafka.instanceflow.producing.InstanceFlowTemplate
import no.novari.flyt.kafka.instanceflow.producing.InstanceFlowTemplateFactory
import no.novari.kafka.topic.EventTopicService
import no.novari.kafka.topic.configuration.EventCleanupFrequency
import no.novari.kafka.topic.configuration.EventTopicConfiguration
import no.novari.kafka.topic.name.EventTopicNameParameters
import no.novari.kafka.topic.name.TopicNamePrefixParameters
import org.springframework.stereotype.Service

@Service
class InstanceDispatchedEventProducerService(
    instanceFlowTemplateFactory: InstanceFlowTemplateFactory,
    eventTopicService: EventTopicService,
    kafkaTopicProperties: KafkaTopicProperties,
) {
    private val eventTopicNameParameters: EventTopicNameParameters =
        EventTopicNameParameters
            .builder()
            .topicNamePrefixParameters(
                TopicNamePrefixParameters
                    .stepBuilder()
                    .orgIdApplicationDefault()
                    .domainContextApplicationDefault()
                    .build(),
            ).eventName("instance-dispatched")
            .build()
    private val instanceFlowTemplate: InstanceFlowTemplate<Any> =
        instanceFlowTemplateFactory.createTemplate(
            Any::class.java,
        )

    init {
        eventTopicService.createOrModifyTopic(
            eventTopicNameParameters,
            EventTopicConfiguration
                .stepBuilder()
                .partitions(PARTITIONS)
                .retentionTime(requireNotNull(kafkaTopicProperties.instanceProcessingEventsRetentionTime))
                .cleanupFrequency(EventCleanupFrequency.NORMAL)
                .build(),
        )
    }

    fun publish(instanceFlowHeaders: InstanceFlowHeaders) {
        instanceFlowTemplate.send(
            InstanceFlowProducerRecord
                .builder<Any>()
                .instanceFlowHeaders(instanceFlowHeaders)
                .topicNameParameters(eventTopicNameParameters)
                .value(null)
                .build(),
        )
    }

    companion object {
        private const val PARTITIONS = 1
    }
}
