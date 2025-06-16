package no.fintlabs.flyt.gateway.application.archive.kafka;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.flyt.gateway.application.archive.dispatch.DispatchService;
import no.fintlabs.flyt.gateway.application.archive.dispatch.model.instance.ArchiveInstance;
import no.fintlabs.flyt.gateway.application.archive.kafka.error.InstanceDispatchingErrorProducerService;
import no.fintlabs.flyt.kafka.event.InstanceFlowEventConsumerFactoryService;
import no.fintlabs.kafka.event.EventConsumerConfiguration;
import no.fintlabs.kafka.event.topic.EventTopicNameParameters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;

@Configuration
@Slf4j
public class InstanceMappedEventConsumerConfiguration {

    @Bean
    public ConcurrentMessageListenerContainer<String, ArchiveInstance> instanceMappedEventConsumer(
            InstanceFlowEventConsumerFactoryService instanceFlowEventConsumerFactoryService,
            DispatchService dispatchService,
            InstanceDispatchedEventProducerService instanceDispatchedEventProducerService,
            InstanceDispatchingErrorProducerService instanceDispatchingErrorProducerService
    ) {
        return instanceFlowEventConsumerFactoryService.createRecordFactory(
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
                EventConsumerConfiguration
                        .builder()
                        .maxPollIntervalMs(1800000)
                        .maxPollRecords(1)
                        .ackMode(ContainerProperties.AckMode.RECORD)
                        .build()
        ).createContainer(
                EventTopicNameParameters.builder()
                        .eventName("instance-mapped")
                        .build()
        );
    }


}
