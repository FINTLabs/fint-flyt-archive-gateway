package no.novari.flyt.archive.gateway.kafka

import no.novari.flyt.archive.gateway.dispatch.DispatchService
import no.novari.flyt.archive.gateway.dispatch.DispatchStatus
import no.novari.flyt.archive.gateway.dispatch.model.instance.ArchiveInstance
import no.novari.flyt.archive.gateway.kafka.error.InstanceDispatchingErrorProducerService
import no.novari.flyt.kafka.instanceflow.consuming.InstanceFlowListenerFactoryService
import no.novari.kafka.consuming.ErrorHandlerConfiguration
import no.novari.kafka.consuming.ErrorHandlerFactory
import no.novari.kafka.consuming.ListenerConfiguration
import no.novari.kafka.topic.name.EventTopicNameParameters
import no.novari.kafka.topic.name.TopicNamePrefixParameters
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import java.time.Duration

@Configuration
class InstanceMappedEventConsumerConfiguration {
    @Bean
    fun instanceMappedEventConsumer(
        instanceFlowListenerFactoryService: InstanceFlowListenerFactoryService,
        dispatchService: DispatchService,
        instanceDispatchedEventProducerService: InstanceDispatchedEventProducerService,
        instanceDispatchingErrorProducerService: InstanceDispatchingErrorProducerService,
        errorHandlerFactory: ErrorHandlerFactory,
    ): ConcurrentMessageListenerContainer<String, ArchiveInstance> =
        instanceFlowListenerFactoryService
            .createRecordListenerContainerFactory(
                ArchiveInstance::class.java,
                { instanceFlowConsumerRecord ->
                    val consumerRecord = instanceFlowConsumerRecord.consumerRecord
                    log.info(
                        "Consumed instance-mapped event topic={} partition={} offset={} key={} headers={}",
                        consumerRecord.topic(),
                        consumerRecord.partition(),
                        consumerRecord.offset(),
                        consumerRecord.key(),
                        instanceFlowConsumerRecord.instanceFlowHeaders,
                    )
                    val dispatchResult =
                        dispatchService.process(
                            instanceFlowConsumerRecord.instanceFlowHeaders,
                            consumerRecord.value(),
                        )
                    when (dispatchResult.status) {
                        DispatchStatus.ACCEPTED -> {
                            instanceDispatchedEventProducerService.publish(
                                instanceFlowConsumerRecord.instanceFlowHeaders
                                    .toBuilder()
                                    .archiveInstanceId(dispatchResult.archiveCaseAndRecordsIds)
                                    .build(),
                            )
                        }

                        DispatchStatus.DECLINED -> {
                            instanceDispatchingErrorProducerService.publishInstanceDispatchDeclinedErrorEvent(
                                instanceFlowConsumerRecord.instanceFlowHeaders,
                                dispatchResult.errorMessage,
                            )
                        }

                        DispatchStatus.FAILED -> {
                            instanceDispatchingErrorProducerService.publishGeneralSystemErrorEvent(
                                instanceFlowConsumerRecord.instanceFlowHeaders,
                                dispatchResult.errorMessage,
                            )
                        }
                    }
                },
                ListenerConfiguration
                    .stepBuilder()
                    .groupIdApplicationDefault()
                    .maxPollRecords(1)
                    .maxPollInterval(Duration.ofMinutes(30))
                    .continueFromPreviousOffsetOnAssignment()
                    .build(),
                errorHandlerFactory.createErrorHandler(
                    ErrorHandlerConfiguration
                        .stepBuilder<ArchiveInstance>()
                        .noRetries()
                        .skipFailedRecords()
                        .build(),
                ),
            ).createContainer(
                EventTopicNameParameters
                    .builder()
                    .topicNamePrefixParameters(
                        TopicNamePrefixParameters
                            .stepBuilder()
                            .orgIdApplicationDefault()
                            .domainContextApplicationDefault()
                            .build(),
                    ).eventName("instance-mapped")
                    .build(),
            )

    companion object {
        private val log = LoggerFactory.getLogger(InstanceMappedEventConsumerConfiguration::class.java)
    }
}
