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
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.RecordDeserializationException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.log.LogAccessor;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.support.TopicPartitionOffset;
import org.springframework.lang.NonNull;

import java.time.Duration;
import java.util.Collection;
import java.util.List;

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
                        .maxPollRecords(1)
                        .maxPollInterval(Duration.ofMinutes(30))
                        .continueFromPreviousOffsetOnAssignment()
                        .build(),
                new SerializationIgnoringErrorHandler(
                        errorHandlerFactory.createErrorHandler(
                                ErrorHandlerConfiguration
                                        .stepBuilder()
                                        .noRetries()
                                        .skipFailedRecords()
                                        .build()
                        )
                )
        ).createContainer(
                EventTopicNameParameters
                        .builder()
                        .topicNamePrefixParameters(TopicNamePrefixParameters
                                .stepBuilder()
                                .orgIdApplicationDefault()
                                .domainContextApplicationDefault()
                                .build()
                        )
                        .eventName("instance-mapped")
                        .build()
        );
    }

    private static class SerializationIgnoringErrorHandler implements CommonErrorHandler {
        private final CommonErrorHandler delegate;

        private SerializationIgnoringErrorHandler(CommonErrorHandler delegate) {
            this.delegate = delegate;
        }

        @Override
        public void handleOtherException(
                @NonNull Exception exception,
                @NonNull Consumer<?, ?> consumer,
                @NonNull MessageListenerContainer container,
                boolean batchListener
        ) {
            RecordDeserializationException recordException = findRecordDeserializationException(exception);
            if (recordException != null) {
                TopicPartition topicPartition = recordException.topicPartition();
                long nextOffset = recordException.offset() + 1;
                log.warn(
                        "Skipping invalid record for {} at offset {}",
                        topicPartition,
                        recordException.offset(),
                        exception
                );
                consumer.seek(topicPartition, nextOffset);
                return;
            }
            delegate.handleOtherException(exception, consumer, container, batchListener);
        }

        @Override
        public boolean handleOne(
                @NonNull Exception exception,
                @NonNull ConsumerRecord<?, ?> record,
                @NonNull Consumer<?, ?> consumer,
                @NonNull MessageListenerContainer container
        ) {
            return delegate.handleOne(exception, record, consumer, container);
        }

        @Override
        public void handleRemaining(
                @NonNull Exception exception,
                @NonNull List<ConsumerRecord<?, ?>> records,
                @NonNull Consumer<?, ?> consumer,
                @NonNull MessageListenerContainer container
        ) {
            delegate.handleRemaining(exception, records, consumer, container);
        }

        @Override
        public void handleBatch(
                @NonNull Exception exception,
                @NonNull ConsumerRecords<?, ?> records,
                @NonNull Consumer<?, ?> consumer,
                @NonNull MessageListenerContainer container,
                @NonNull Runnable invokeListener
        ) {
            delegate.handleBatch(exception, records, consumer, container, invokeListener);
        }

        @Override
        public boolean seeksAfterHandling() {
            return delegate.seeksAfterHandling();
        }

        @Override
        public boolean deliveryAttemptHeader() {
            return delegate.deliveryAttemptHeader();
        }

        @Override
        @NonNull
        public LogAccessor logger() {
            return delegate.logger();
        }

        @Override
        public int deliveryAttempt(TopicPartitionOffset topicPartitionOffset) {
            return delegate.deliveryAttempt(topicPartitionOffset);
        }

        @Override
        public void clearThreadState() {
            delegate.clearThreadState();
        }

        @Override
        public boolean isAckAfterHandle() {
            return delegate.isAckAfterHandle();
        }

        @Override
        public void setAckAfterHandle(boolean ackAfterHandle) {
            delegate.setAckAfterHandle(ackAfterHandle);
        }

        @Override
        public void onPartitionsAssigned(
                @NonNull Consumer<?, ?> consumer,
                @NonNull Collection<TopicPartition> partitions,
                @NonNull Runnable callback
        ) {
            delegate.onPartitionsAssigned(consumer, partitions, callback);
        }

        private static RecordDeserializationException findRecordDeserializationException(Exception exception) {
            if (exception instanceof RecordDeserializationException recordException) {
                return recordException;
            }
            Throwable cause = exception.getCause();
            if (cause instanceof RecordDeserializationException recordException) {
                return recordException;
            }
            return null;
        }
    }

}
