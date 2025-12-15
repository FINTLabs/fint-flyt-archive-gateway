package no.novari.flyt.archive.gateway.resource.sak;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.arkiv.noark.SakResource;
import no.novari.kafka.consuming.ListenerConfiguration;
import no.novari.kafka.requestreply.RequestProducerRecord;
import no.novari.kafka.requestreply.RequestTemplate;
import no.novari.kafka.requestreply.RequestTemplateFactory;
import no.novari.kafka.requestreply.topic.ReplyTopicService;
import no.novari.kafka.requestreply.topic.configuration.ReplyTopicConfiguration;
import no.novari.kafka.requestreply.topic.name.ReplyTopicNameParameters;
import no.novari.kafka.requestreply.topic.name.RequestTopicNameParameters;
import no.novari.kafka.topic.name.TopicNamePrefixParameters;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@Slf4j
public class CaseRequestService {

    private final RequestTopicNameParameters requestTopicNameParameters;
    private final RequestTemplate<String, SakResource> requestTemplate;

    private static final Duration RETENTION_TIME = Duration.ofMinutes(10);
    public static final Duration REPLY_TIMEOUT = Duration.ofSeconds(60);

    public CaseRequestService(
            @Value("${novari.kafka.application-id}") String applicationId,
            ReplyTopicService replyTopicService,
            RequestTemplateFactory requestTemplateFactory
    ) {
        requestTopicNameParameters = RequestTopicNameParameters
                .builder()
                .topicNamePrefixParameters(TopicNamePrefixParameters
                        .stepBuilder()
                        .orgIdApplicationDefault()
                        .domainContextApplicationDefault()
                        .build()
                )
                .resourceName("arkiv-noark-sak")
                .parameterName("mappeid")
                .build();

        ReplyTopicNameParameters replyTopicNameParameters = ReplyTopicNameParameters
                .builder()
                .topicNamePrefixParameters(TopicNamePrefixParameters
                        .stepBuilder()
                        .orgIdApplicationDefault()
                        .domainContextApplicationDefault()
                        .build()
                )
                .applicationId(applicationId)
                .resourceName("arkiv-noark-sak")
                .build();

        replyTopicService.createOrModifyTopic(replyTopicNameParameters, ReplyTopicConfiguration
                .builder()
                .retentionTime(RETENTION_TIME)
                .build());

        this.requestTemplate = requestTemplateFactory.createTemplate(
                replyTopicNameParameters,
                String.class,
                SakResource.class,
                REPLY_TIMEOUT,
                ListenerConfiguration
                        .stepBuilder()
                        .groupIdApplicationDefault()
                        .maxPollRecordsKafkaDefault()
                        .maxPollIntervalKafkaDefault()
                        .continueFromPreviousOffsetOnAssignment()
                        .build()
        );
    }

    public Optional<SakResource> getByMappeId(String mappeId) {
        return Optional.ofNullable(
                requestTemplate.requestAndReceive(
                        RequestProducerRecord.<String>builder()
                                .topicNameParameters(requestTopicNameParameters)
                                .value(mappeId)
                                .build()
                ).value()
        );
    }

}
