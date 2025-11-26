package no.novari.flyt.archive.gateway.resource;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.arkiv.noark.JournalpostResource;
import no.fint.model.resource.arkiv.noark.SakResource;
import no.novari.flyt.archive.gateway.resource.web.FintArchiveResourceClient;
import no.novari.kafka.consuming.ErrorHandlerConfiguration;
import no.novari.kafka.consuming.ErrorHandlerFactory;
import no.novari.kafka.requestreply.ReplyProducerRecord;
import no.novari.kafka.requestreply.RequestListenerConfiguration;
import no.novari.kafka.requestreply.RequestListenerContainerFactory;
import no.novari.kafka.requestreply.topic.RequestTopicService;
import no.novari.kafka.requestreply.topic.configuration.RequestTopicConfiguration;
import no.novari.kafka.requestreply.topic.name.RequestTopicNameParameters;
import no.novari.kafka.topic.name.TopicNamePrefixParameters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Configuration
@Slf4j
public class CaseRequestConfiguration {

    private final RequestListenerContainerFactory requestListenerContainerFactory;
    private final ErrorHandlerFactory errorHandlerFactory;
    private final RequestTopicService requestTopicService;
    private final FintArchiveResourceClient fintArchiveResourceClient;

    private static final Duration RETENTION_TIME = Duration.ofMinutes(10);

    public CaseRequestConfiguration(
            RequestListenerContainerFactory requestListenerContainerFactory,
            ErrorHandlerFactory errorHandlerFactory,
            RequestTopicService requestTopicService,
            FintArchiveResourceClient fintArchiveResourceClient
    ) {
        this.requestListenerContainerFactory = requestListenerContainerFactory;
        this.errorHandlerFactory = errorHandlerFactory;
        this.requestTopicService = requestTopicService;
        this.fintArchiveResourceClient = fintArchiveResourceClient;
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, String> caseRequestByMappeIdConsumer() {
        RequestTopicNameParameters topicNameParameters = RequestTopicNameParameters
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

        requestTopicService.createOrModifyTopic(topicNameParameters, RequestTopicConfiguration
                .builder()
                .retentionTime(RETENTION_TIME)
                .build()
        );

        return requestListenerContainerFactory.createRecordConsumerFactory(
                String.class,
                SakResource.class,
                (consumerRecord) -> handleCaseRequestByMappeId(consumerRecord.value()),
                RequestListenerConfiguration
                        .stepBuilder(String.class)
                        .maxPollRecordsKafkaDefault()
                        .maxPollIntervalKafkaDefault()
                        .build(),
                errorHandlerFactory.createErrorHandler(ErrorHandlerConfiguration
                        .stepBuilder()
                        .noRetries()
                        .skipFailedRecords()
                        .build()
                )
        ).createContainer(topicNameParameters);
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, String> caseRequestByArchiveInstanceIdConsumer() {
        RequestTopicNameParameters topicNameParameters = RequestTopicNameParameters
                .builder()
                .topicNamePrefixParameters(TopicNamePrefixParameters
                        .stepBuilder()
                        .orgIdApplicationDefault()
                        .domainContextApplicationDefault()
                        .build()
                )
                .resourceName("arkiv-noark-sak-with-filtered-journalposts")
                .parameterName("archive-instance-id")
                .build();

        requestTopicService.createOrModifyTopic(topicNameParameters, RequestTopicConfiguration
                .builder()
                .retentionTime(RETENTION_TIME)
                .build()
        );

        return requestListenerContainerFactory.createRecordConsumerFactory(
                String.class,
                SakResource.class,
                (consumerRecord) -> handleCaseRequestByArchiveInstanceId(consumerRecord.value()),
                RequestListenerConfiguration
                        .stepBuilder(String.class)
                        .maxPollRecordsKafkaDefault()
                        .maxPollIntervalKafkaDefault()
                        .build(),
                errorHandlerFactory.createErrorHandler(
                        ErrorHandlerConfiguration
                                .stepBuilder()
                                .noRetries()
                                .skipFailedRecords()
                                .build()
                )
        ).createContainer(topicNameParameters);
    }

    @Getter
    @Builder
    private static class CaseAndJournalpostIds {
        private final String caseId;
        private final List<Long> journalpostIds;
    }

    private CaseAndJournalpostIds extractCaseAndJournalpostIds(String archiveInstanceId) {
        String[] splitArchiveInstanceId = archiveInstanceId.split("-");
        String caseId = splitArchiveInstanceId[0];
        List<Long> journalpostIds = splitArchiveInstanceId.length == 1
                ? List.of()
                : Arrays.stream(
                        splitArchiveInstanceId[1]
                                .replace("[", "")
                                .replace("]", "")
                                .split(",")
                )
                .map(Long::parseLong)
                .toList();
        return CaseAndJournalpostIds
                .builder()
                .caseId(caseId)
                .journalpostIds(journalpostIds)
                .build();
    }

    private ReplyProducerRecord<SakResource> handleCaseRequestByMappeId(String mappeId) {
        try {
            SakResource sakResource = fintArchiveResourceClient
                    .getResource("/arkiv/noark/sak/mappeid/" + mappeId, SakResource.class)
                    .block();
            return ReplyProducerRecord.<SakResource>builder()
                    .value(sakResource)
                    .build();
        } catch (RuntimeException e) {
            log.error("Could not find case with id={}", mappeId, e);
            return ReplyProducerRecord.<SakResource>builder()
                    .value(null)
                    .build();
        }
    }

    private ReplyProducerRecord<SakResource> handleCaseRequestByArchiveInstanceId(String archiveInstanceId) {
        CaseAndJournalpostIds caseAndJournalpostIds = extractCaseAndJournalpostIds(archiveInstanceId);
        try {
            SakResource sakResource = fintArchiveResourceClient
                    .getResource("/arkiv/noark/sak/mappeid/" + caseAndJournalpostIds.getCaseId(), SakResource.class)
                    .block();
            if (sakResource != null) {
                List<JournalpostResource> filteredJournalposts = sakResource.getJournalpost()
                        .stream()
                        .filter(journalpostResource -> caseAndJournalpostIds.getJournalpostIds()
                                .contains(journalpostResource.getJournalPostnummer()))
                        .toList();
                sakResource.setJournalpost(filteredJournalposts);
            }
            return ReplyProducerRecord.<SakResource>builder()
                    .value(sakResource)
                    .build();
        } catch (RuntimeException e) {
            log.error("Could not find case with archive instance id={}", archiveInstanceId, e);
            return ReplyProducerRecord.<SakResource>builder()
                    .value(null)
                    .build();
        }
    }

}
