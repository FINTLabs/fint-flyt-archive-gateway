package no.novari.flyt.archive.gateway.resource.sak

import no.novari.fint.model.resource.arkiv.noark.SakResource
import no.novari.kafka.consuming.ListenerConfiguration
import no.novari.kafka.requestreply.RequestProducerRecord
import no.novari.kafka.requestreply.RequestTemplate
import no.novari.kafka.requestreply.RequestTemplateFactory
import no.novari.kafka.requestreply.topic.ReplyTopicService
import no.novari.kafka.requestreply.topic.configuration.ReplyTopicConfiguration
import no.novari.kafka.requestreply.topic.name.ReplyTopicNameParameters
import no.novari.kafka.requestreply.topic.name.RequestTopicNameParameters
import no.novari.kafka.topic.name.TopicNamePrefixParameters
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class CaseRequestService(
    @Value("\${novari.kafka.application-id}") applicationId: String,
    replyTopicService: ReplyTopicService,
    requestTemplateFactory: RequestTemplateFactory,
) {
    private val requestTopicNameParameters =
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

    private val requestTemplate: RequestTemplate<String, SakResource>

    init {
        val replyTopicNameParameters =
            ReplyTopicNameParameters
                .builder()
                .topicNamePrefixParameters(
                    TopicNamePrefixParameters
                        .stepBuilder()
                        .orgIdApplicationDefault()
                        .domainContextApplicationDefault()
                        .build(),
                ).applicationId(applicationId)
                .resourceName("arkiv-noark-sak")
                .build()

        replyTopicService.createOrModifyTopic(
            replyTopicNameParameters,
            ReplyTopicConfiguration.builder().retentionTime(RETENTION_TIME).build(),
        )

        requestTemplate =
            requestTemplateFactory.createTemplate(
                replyTopicNameParameters,
                String::class.java,
                SakResource::class.java,
                REPLY_TIMEOUT,
                ListenerConfiguration
                    .stepBuilder()
                    .groupIdApplicationDefault()
                    .maxPollRecordsKafkaDefault()
                    .maxPollIntervalKafkaDefault()
                    .continueFromPreviousOffsetOnAssignment()
                    .build(),
            )
    }

    fun getByMappeId(mappeId: String): SakResource? =
        requestTemplate
            .requestAndReceive(
                RequestProducerRecord
                    .builder<String>()
                    .topicNameParameters(
                        requestTopicNameParameters,
                    ).value(mappeId)
                    .build(),
            ).value()

    companion object {
        private val RETENTION_TIME: Duration = Duration.ofMinutes(10)
        val REPLY_TIMEOUT: Duration = Duration.ofSeconds(60)
    }
}
