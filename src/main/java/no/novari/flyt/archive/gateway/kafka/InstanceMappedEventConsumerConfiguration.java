package no.novari.flyt.archive.gateway.kafka;

import lombok.extern.slf4j.Slf4j;
import no.novari.flyt.archive.gateway.dispatch.DispatchService;
import no.novari.flyt.archive.gateway.dispatch.model.instance.ArchiveInstance;
import no.novari.flyt.archive.gateway.kafka.error.InstanceDispatchingErrorProducerService;
import no.novari.flyt.kafka.instanceflow.consuming.InstanceFlowListenerFactoryService;
import no.novari.kafka.consuming.ErrorHandlerConfiguration;
import no.novari.kafka.consuming.ErrorHandlerFactory;
import no.novari.kafka.consuming.ListenerConfiguration;
import no.novari.kafka.topic.name.EventTopicNameParameters;
import no.novari.kafka.topic.name.TopicNamePrefixParameters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

import java.time.Duration;

@Configuration
@Slf4j
public class InstanceMappedEventConsumerConfiguration {

    @Bean
    public ConcurrentMessageListenerContainer<String, ArchiveInstance> instanceMappedEventConsumer(
            InstanceFlowListenerFactoryService instanceFlowListenerFactoryService,
            DispatchService dispatchService,
            InstanceDispatchedEventProducerService instanceDispatchedEventProducerService,
            InstanceDispatchingErrorProducerService instanceDispatchingErrorProducerService,
            ErrorHandlerFactory errorHandlerFactory
    ) {
        return instanceFlowListenerFactoryService.createRecordListenerContainerFactory(
                ArchiveInstance.class,
                instanceFlowConsumerRecord ->
                        dispatchService.process(
                                instanceFlowConsumerRecord.getInstanceFlowHeaders(),
                                instanceFlowConsumerRecord.getConsumerRecord().value()
                        ).doOnNext(dispatchResult -> {
                            switch (dispatchResult.getStatus()) {
                                case ACCEPTED -> instanceDispatchedEventProducerService.publish(
                                        instanceFlowConsumerRecord.getInstanceFlowHeaders()
                                                .toBuilder()
                                                .archiveInstanceId(dispatchResult.getArchiveCaseAndRecordsIds())
                                                .build()
                                );
                                case DECLINED ->
                                        instanceDispatchingErrorProducerService.publishInstanceDispatchDeclinedErrorEvent(
                                                instanceFlowConsumerRecord.getInstanceFlowHeaders(),
                                                dispatchResult.getErrorMessage()
                                        );
                                case FAILED -> instanceDispatchingErrorProducerService.publishGeneralSystemErrorEvent(
                                        instanceFlowConsumerRecord.getInstanceFlowHeaders(),
                                        dispatchResult.getErrorMessage()
                                );
                            }
                        }).block(),
                ListenerConfiguration
                        .stepBuilder()
                        .groupIdApplicationDefault()
                        // TODO: Verify this is correct
                        .maxPollRecords(1)
                        // TODO: Verify this is correct
                        .maxPollInterval(Duration.ofMillis(1800000))
                        .continueFromPreviousOffsetOnAssignment()
                        .build(),
                errorHandlerFactory.createErrorHandler(
                        ErrorHandlerConfiguration
                                .stepBuilder()
                                .noRetries()
                                .skipFailedRecords()
                                .build()
                )
        ).createContainer(
                EventTopicNameParameters
                        .builder()
                        .topicNamePrefixParameters(TopicNamePrefixParameters
                                .builder()
                                .orgIdApplicationDefault()
                                .domainContextApplicationDefault()
                                .build()
                        )
                        .eventName("instance-mapped")
                        .build()
        );
    }


}
