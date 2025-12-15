package no.novari.flyt.archive.gateway.kafka.error;

import no.novari.flyt.archive.gateway.kafka.KafkaTopicProperties;
import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeaders;
import no.novari.flyt.kafka.instanceflow.producing.InstanceFlowProducerRecord;
import no.novari.flyt.kafka.instanceflow.producing.InstanceFlowTemplate;
import no.novari.flyt.kafka.instanceflow.producing.InstanceFlowTemplateFactory;
import no.novari.flyt.kafka.model.Error;
import no.novari.flyt.kafka.model.ErrorCollection;
import no.novari.kafka.topic.ErrorEventTopicService;
import no.novari.kafka.topic.configuration.EventCleanupFrequency;
import no.novari.kafka.topic.configuration.EventTopicConfiguration;
import no.novari.kafka.topic.name.ErrorEventTopicNameParameters;
import no.novari.kafka.topic.name.TopicNamePrefixParameters;
import org.springframework.stereotype.Service;

import java.util.Map;

import static no.novari.flyt.archive.gateway.kafka.error.ErrorCode.GENERAL_SYSTEM_ERROR;
import static no.novari.flyt.archive.gateway.kafka.error.ErrorCode.INSTANCE_DISPATCH_DECLINED_ERROR;

@Service
public class InstanceDispatchingErrorProducerService {

    private final ErrorEventTopicNameParameters errorEventTopicNameParameters;
    private final InstanceFlowTemplate<ErrorCollection> instanceFlowTemplate;

    private static final int PARTITIONS = 1;

    public InstanceDispatchingErrorProducerService(
            InstanceFlowTemplateFactory instanceFlowTemplateFactory,
            ErrorEventTopicService errorEventTopicService,
            KafkaTopicProperties kafkaTopicProperties
    ) {
        this.instanceFlowTemplate = instanceFlowTemplateFactory.createTemplate(ErrorCollection.class);
        errorEventTopicNameParameters = ErrorEventTopicNameParameters
                .builder()
                .topicNamePrefixParameters(TopicNamePrefixParameters
                        .stepBuilder()
                        .orgIdApplicationDefault()
                        .domainContextApplicationDefault()
                        .build()
                )
                .errorEventName("instance-dispatching-error")
                .build();
        errorEventTopicService.createOrModifyTopic(errorEventTopicNameParameters, EventTopicConfiguration
                .stepBuilder()
                .partitions(PARTITIONS)
                .retentionTime(kafkaTopicProperties.getInstanceProcessingEventsRetentionTime())
                .cleanupFrequency(EventCleanupFrequency.NORMAL)
                .build()
        );
    }

    public void publishInstanceDispatchDeclinedErrorEvent(
            InstanceFlowHeaders instanceFlowHeaders,
            String errorMessage
    ) {
        instanceFlowTemplate.send(
                InstanceFlowProducerRecord
                        .<ErrorCollection>builder()
                        .instanceFlowHeaders(instanceFlowHeaders)
                        .topicNameParameters(errorEventTopicNameParameters)
                        .value(
                                new ErrorCollection(
                                        Error
                                                .builder()
                                                .errorCode(INSTANCE_DISPATCH_DECLINED_ERROR.getCode())
                                                .args(Map.of("errorMessage", errorMessage))
                                                .build()
                                )
                        )
                        .build()
        );
    }

    public void publishGeneralSystemErrorEvent(
            InstanceFlowHeaders instanceFlowHeaders
    ) {
        publishGeneralSystemErrorEvent(instanceFlowHeaders, "");
    }

    public void publishGeneralSystemErrorEvent(
            InstanceFlowHeaders instanceFlowHeaders,
            String errorMessage
    ) {
        String safeErrorMessage = (errorMessage != null && !errorMessage.isEmpty()) ? errorMessage : "Unknown error occurred";

        instanceFlowTemplate.send(
                InstanceFlowProducerRecord
                        .<ErrorCollection>builder()
                        .instanceFlowHeaders(instanceFlowHeaders)
                        .topicNameParameters(errorEventTopicNameParameters)
                        .value(
                                new ErrorCollection(
                                        Error
                                                .builder()
                                                .errorCode(GENERAL_SYSTEM_ERROR.getCode())
                                                .args(Map.of("errorMessage", safeErrorMessage))
                                                .build()
                                )
                        )
                        .build()
        );
    }

}
