package no.novari.flyt.archive.gateway.kafka

import io.github.oshai.kotlinlogging.KotlinLogging
import no.novari.flyt.archive.gateway.dispatch.DispatchService
import no.novari.flyt.archive.gateway.dispatch.DispatchStatus
import no.novari.flyt.archive.gateway.dispatch.model.instance.ArchiveInstance
import no.novari.flyt.archive.gateway.kafka.error.InstanceErrorEventProducerService
import no.novari.flyt.kafka.instanceflow.headers.INSTANCE_FLOW_HEADERS_KEY
import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeaders
import no.novari.kafka.starter.topic.EventTopicName
import no.novari.kafka.starter.topic.KafkaTopics
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.listener.CommonErrorHandler
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Component
import org.springframework.util.backoff.FixedBackOff

@Configuration
class InstanceMappedEventConsumerConfiguration {
    @Bean
    fun instanceMappedTopic(topics: KafkaTopics): EventTopicName = topics.event("instance-mapped")

    /** One attempt, then log and skip the record. */
    @Bean
    fun kafkaErrorHandler(): CommonErrorHandler = DefaultErrorHandler(FixedBackOff(0, 0))
}

@Component
class InstanceMappedEventListener(
    private val dispatchService: DispatchService,
    private val instanceDispatchedEventProducerService: InstanceDispatchedEventProducerService,
    private val instanceErrorEventProducerService: InstanceErrorEventProducerService,
) {
    // One instance per poll, and up to 30 minutes to dispatch it.
    @KafkaListener(
        topics = ["#{@instanceMappedTopic.name}"],
        properties = ["max.poll.records=1", "max.poll.interval.ms=1800000"],
    )
    fun on(
        archiveInstance: ArchiveInstance,
        @Header(INSTANCE_FLOW_HEADERS_KEY) headers: InstanceFlowHeaders,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
        @Header(KafkaHeaders.RECEIVED_KEY, required = false) key: String?,
    ) {
        log.atInfo {
            message = "Consumed instance-mapped event topic={} partition={} offset={} key={} headers={}"
            arguments = arrayOf(topic, partition, offset, key, headers)
        }
        val dispatchResult = dispatchService.process(headers, archiveInstance)
        when (dispatchResult.status) {
            DispatchStatus.ACCEPTED -> {
                instanceDispatchedEventProducerService.publish(
                    headers.copy(archiveInstanceId = dispatchResult.archiveCaseAndRecordsIds),
                )
            }

            DispatchStatus.DECLINED -> {
                instanceErrorEventProducerService.publishInstanceDispatchDeclinedErrorEvent(
                    headers,
                    dispatchResult.errorMessage,
                )
            }

            DispatchStatus.FAILED -> {
                instanceErrorEventProducerService.publishGeneralSystemErrorEvent(headers, dispatchResult.errorMessage)
            }
        }
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}
