package no.novari.flyt.archive.gateway.kafka.error

import no.novari.flyt.archive.gateway.kafka.KafkaTopicProperties
import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeaders
import no.novari.flyt.kafka.instanceflow.producing.InstanceFlowProducerRecord
import no.novari.flyt.kafka.instanceflow.producing.InstanceFlowTemplate
import no.novari.flyt.kafka.instanceflow.producing.InstanceFlowTemplateFactory
import no.novari.flyt.kafka.model.Error
import no.novari.flyt.kafka.model.ErrorCollection
import no.novari.kafka.topic.ErrorEventTopicService
import no.novari.kafka.topic.configuration.EventCleanupFrequency
import no.novari.kafka.topic.configuration.EventTopicConfiguration
import no.novari.kafka.topic.name.ErrorEventTopicNameParameters
import no.novari.kafka.topic.name.TopicNamePrefixParameters
import org.springframework.stereotype.Service

@Service
class InstanceDispatchingErrorProducerService(
    instanceFlowTemplateFactory: InstanceFlowTemplateFactory,
    errorEventTopicService: ErrorEventTopicService,
    kafkaTopicProperties: KafkaTopicProperties,
) {
    private val errorEventTopicNameParameters: ErrorEventTopicNameParameters =
        ErrorEventTopicNameParameters
            .builder()
            .topicNamePrefixParameters(
                TopicNamePrefixParameters
                    .stepBuilder()
                    .orgIdApplicationDefault()
                    .domainContextApplicationDefault()
                    .build(),
            ).errorEventName("instance-dispatching-error")
            .build()
    private val instanceFlowTemplate: InstanceFlowTemplate<ErrorCollection> =
        instanceFlowTemplateFactory.createTemplate(ErrorCollection::class.java)

    init {
        errorEventTopicService.createOrModifyTopic(
            errorEventTopicNameParameters,
            EventTopicConfiguration
                .stepBuilder()
                .partitions(PARTITIONS)
                .retentionTime(requireNotNull(kafkaTopicProperties.instanceProcessingEventsRetentionTime))
                .cleanupFrequency(EventCleanupFrequency.NORMAL)
                .build(),
        )
    }

    fun publishInstanceDispatchDeclinedErrorEvent(
        instanceFlowHeaders: InstanceFlowHeaders,
        errorMessage: String?,
    ) {
        instanceFlowTemplate.send(
            InstanceFlowProducerRecord
                .builder<ErrorCollection>()
                .instanceFlowHeaders(instanceFlowHeaders)
                .topicNameParameters(errorEventTopicNameParameters)
                .value(
                    ErrorCollection(
                        Error
                            .builder()
                            .errorCode(ErrorCode.INSTANCE_DISPATCH_DECLINED_ERROR.getCode())
                            .args(mapOf("errorMessage" to errorMessage))
                            .build(),
                    ),
                ).build(),
        )
    }

    fun publishGeneralSystemErrorEvent(instanceFlowHeaders: InstanceFlowHeaders) {
        publishGeneralSystemErrorEvent(instanceFlowHeaders, "")
    }

    fun publishGeneralSystemErrorEvent(
        instanceFlowHeaders: InstanceFlowHeaders,
        errorMessage: String?,
    ) {
        val safeErrorMessage = if (!errorMessage.isNullOrEmpty()) errorMessage else "Unknown error occurred"

        instanceFlowTemplate.send(
            InstanceFlowProducerRecord
                .builder<ErrorCollection>()
                .instanceFlowHeaders(instanceFlowHeaders)
                .topicNameParameters(errorEventTopicNameParameters)
                .value(
                    ErrorCollection(
                        Error
                            .builder()
                            .errorCode(ErrorCode.GENERAL_SYSTEM_ERROR.getCode())
                            .args(mapOf("errorMessage" to safeErrorMessage))
                            .build(),
                    ),
                ).build(),
        )
    }

    companion object {
        private const val PARTITIONS = 1
    }
}
