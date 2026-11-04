package no.novari.flyt.archive.gateway.resource

import no.novari.fint.model.resource.arkiv.noark.SakResource
import no.novari.flyt.archive.gateway.resource.sak.CaseService
import no.novari.kafka.consuming.ErrorHandlerConfiguration
import no.novari.kafka.consuming.ErrorHandlerFactory
import no.novari.kafka.requestreply.ReplyProducerRecord
import no.novari.kafka.requestreply.RequestListenerConfiguration
import no.novari.kafka.requestreply.RequestListenerContainerFactory
import no.novari.kafka.requestreply.topic.RequestTopicService
import no.novari.kafka.requestreply.topic.configuration.RequestTopicConfiguration
import no.novari.kafka.requestreply.topic.name.RequestTopicNameParameters
import no.novari.kafka.topic.name.TopicNamePrefixParameters
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import java.time.Duration

@Configuration
class CaseRequestConfiguration(
    private val requestListenerContainerFactory: RequestListenerContainerFactory,
    private val errorHandlerFactory: ErrorHandlerFactory,
    private val requestTopicService: RequestTopicService,
    private val caseService: CaseService,
) {
    @Bean
    fun caseRequestByMappeIdConsumer(): ConcurrentMessageListenerContainer<String, String> {
        val topicNameParameters =
            RequestTopicNameParameters
                .builder()
                .topicNamePrefixParameters(
                    TopicNamePrefixParameters
                        .stepBuilder()
                        .orgIdApplicationDefault()
                        .domainContextApplicationDefault()
                        .build(),
                ).resourceName("arkiv-noark-sak")
                .parameterName("mappeid")
                .build()

        requestTopicService.createOrModifyTopic(
            topicNameParameters,
            RequestTopicConfiguration.builder().retentionTime(RETENTION_TIME).build(),
        )

        return requestListenerContainerFactory
            .createRecordConsumerFactory(
                String::class.java,
                SakResource::class.java,
                { consumerRecord -> handleCaseRequestByMappeId(consumerRecord.value()) },
                RequestListenerConfiguration
                    .stepBuilder(String::class.java)
                    .maxPollRecordsKafkaDefault()
                    .maxPollIntervalKafkaDefault()
                    .build(),
                errorHandlerFactory.createErrorHandler(
                    ErrorHandlerConfiguration
                        .stepBuilder<String>()
                        .noRetries()
                        .skipFailedRecords()
                        .build(),
                ),
            ).createContainer(topicNameParameters)
    }

    @Bean
    fun caseRequestByArchiveInstanceIdConsumer(): ConcurrentMessageListenerContainer<String, String> {
        val topicNameParameters =
            RequestTopicNameParameters
                .builder()
                .topicNamePrefixParameters(
                    TopicNamePrefixParameters
                        .stepBuilder()
                        .orgIdApplicationDefault()
                        .domainContextApplicationDefault()
                        .build(),
                ).resourceName("arkiv-noark-sak-with-filtered-journalposts")
                .parameterName("archive-instance-id")
                .build()

        requestTopicService.createOrModifyTopic(
            topicNameParameters,
            RequestTopicConfiguration.builder().retentionTime(RETENTION_TIME).build(),
        )

        return requestListenerContainerFactory
            .createRecordConsumerFactory(
                String::class.java,
                SakResource::class.java,
                { consumerRecord -> handleCaseRequestByArchiveInstanceId(consumerRecord.value()) },
                RequestListenerConfiguration
                    .stepBuilder(String::class.java)
                    .maxPollRecordsKafkaDefault()
                    .maxPollIntervalKafkaDefault()
                    .build(),
                errorHandlerFactory.createErrorHandler(
                    ErrorHandlerConfiguration
                        .stepBuilder<String>()
                        .noRetries()
                        .skipFailedRecords()
                        .build(),
                ),
            ).createContainer(topicNameParameters)
    }

    private fun handleCaseRequestByMappeId(mappeId: String): ReplyProducerRecord<SakResource> =
        ReplyProducerRecord.builder<SakResource>().value(caseService.getByMappeId(mappeId)).build()

    private fun handleCaseRequestByArchiveInstanceId(archiveInstanceId: String): ReplyProducerRecord<SakResource> =
        ReplyProducerRecord
            .builder<SakResource>()
            .value(caseService.getByArchiveInstanceIdWithFilteredJournalposts(archiveInstanceId))
            .build()

    companion object {
        private val RETENTION_TIME: Duration = Duration.ofMinutes(10)
    }
}
