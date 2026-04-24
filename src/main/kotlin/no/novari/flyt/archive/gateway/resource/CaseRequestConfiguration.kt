package no.novari.flyt.archive.gateway.resource

import no.novari.fint.model.resource.arkiv.noark.JournalpostResource
import no.novari.fint.model.resource.arkiv.noark.SakResource
import no.novari.flyt.archive.gateway.resource.web.FintArchiveResourceClient
import no.novari.kafka.consuming.ErrorHandlerConfiguration
import no.novari.kafka.consuming.ErrorHandlerFactory
import no.novari.kafka.requestreply.ReplyProducerRecord
import no.novari.kafka.requestreply.RequestListenerConfiguration
import no.novari.kafka.requestreply.RequestListenerContainerFactory
import no.novari.kafka.requestreply.topic.RequestTopicService
import no.novari.kafka.requestreply.topic.configuration.RequestTopicConfiguration
import no.novari.kafka.requestreply.topic.name.RequestTopicNameParameters
import no.novari.kafka.topic.name.TopicNamePrefixParameters
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import java.time.Duration

@Configuration
class CaseRequestConfiguration(
    private val requestListenerContainerFactory: RequestListenerContainerFactory,
    private val errorHandlerFactory: ErrorHandlerFactory,
    private val requestTopicService: RequestTopicService,
    private val fintArchiveResourceClient: FintArchiveResourceClient,
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

    private fun extractCaseAndJournalpostIds(archiveInstanceId: String): CaseAndJournalpostIds {
        val splitArchiveInstanceId = archiveInstanceId.split("-")
        val caseId = splitArchiveInstanceId.first()
        val journalpostIds =
            if (splitArchiveInstanceId.size == 1) {
                emptyList()
            } else {
                splitArchiveInstanceId[1]
                    .replace("[", "")
                    .replace("]", "")
                    .split(",")
                    .filter(String::isNotBlank)
                    .map(String::toLong)
            }

        return CaseAndJournalpostIds(caseId = caseId, journalpostIds = journalpostIds)
    }

    private fun handleCaseRequestByMappeId(mappeId: String): ReplyProducerRecord<SakResource> =
        try {
            val sakResource =
                fintArchiveResourceClient
                    .getResource("/arkiv/noark/sak/mappeid/$mappeId", SakResource::class.java)
                    .block()
            ReplyProducerRecord.builder<SakResource>().value(sakResource).build()
        } catch (error: RuntimeException) {
            log.error("Could not find case with id={}", mappeId, error)
            ReplyProducerRecord.builder<SakResource>().value(null).build()
        }

    private fun handleCaseRequestByArchiveInstanceId(archiveInstanceId: String): ReplyProducerRecord<SakResource> {
        val caseAndJournalpostIds = extractCaseAndJournalpostIds(archiveInstanceId)
        return try {
            val sakResource =
                fintArchiveResourceClient
                    .getResource("/arkiv/noark/sak/mappeid/${caseAndJournalpostIds.caseId}", SakResource::class.java)
                    .block()

            sakResource?.let { resource ->
                val journalposts: List<JournalpostResource> = resource.journalpost?.toList() ?: emptyList()
                val filteredJournalposts =
                    journalposts.filter { journalpostResource ->
                        caseAndJournalpostIds.journalpostIds.contains(journalpostResource.journalPostnummer)
                    }
                resource.journalpost = filteredJournalposts
            }

            ReplyProducerRecord.builder<SakResource>().value(sakResource).build()
        } catch (error: RuntimeException) {
            log.error("Could not find case with archive instance id={}", archiveInstanceId, error)
            ReplyProducerRecord.builder<SakResource>().value(null).build()
        }
    }

    private data class CaseAndJournalpostIds(
        val caseId: String,
        val journalpostIds: List<Long>,
    )

    companion object {
        private val log = LoggerFactory.getLogger(CaseRequestConfiguration::class.java)
        private val RETENTION_TIME: Duration = Duration.ofMinutes(10)
    }
}
