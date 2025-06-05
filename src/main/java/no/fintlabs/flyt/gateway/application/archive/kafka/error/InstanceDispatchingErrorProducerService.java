package no.fintlabs.flyt.gateway.application.archive.kafka.error;

import no.fintlabs.flyt.gateway.application.archive.kafka.KafkaTopicProperties;
import no.fintlabs.flyt.kafka.event.error.InstanceFlowErrorEventProducer;
import no.fintlabs.flyt.kafka.event.error.InstanceFlowErrorEventProducerRecord;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;
import no.fintlabs.kafka.event.error.Error;
import no.fintlabs.kafka.event.error.ErrorCollection;
import no.fintlabs.kafka.event.error.topic.ErrorEventTopicNameParameters;
import no.fintlabs.kafka.event.error.topic.ErrorEventTopicService;
import org.springframework.stereotype.Service;

import java.util.Map;

import static no.fintlabs.flyt.gateway.application.archive.kafka.error.ErrorCode.GENERAL_SYSTEM_ERROR;
import static no.fintlabs.flyt.gateway.application.archive.kafka.error.ErrorCode.INSTANCE_DISPATCH_DECLINED_ERROR;

@Service
public class InstanceDispatchingErrorProducerService {

    private final InstanceFlowErrorEventProducer errorEventProducer;
    private final ErrorEventTopicNameParameters errorEventTopicNameParameters;

    public InstanceDispatchingErrorProducerService(
            InstanceFlowErrorEventProducer errorEventProducer,
            ErrorEventTopicService errorEventTopicService,
            KafkaTopicProperties kafkaTopicProperties
    ) {
        this.errorEventProducer = errorEventProducer;
        errorEventTopicNameParameters = ErrorEventTopicNameParameters
                .builder()
                .errorEventName("instance-dispatching-error")
                .build();
        errorEventTopicService.ensureTopic(errorEventTopicNameParameters, kafkaTopicProperties.getInstanceProcessingEventsRetentionTimeMs());
    }

    public void publishInstanceDispatchDeclinedErrorEvent(
            InstanceFlowHeaders instanceFlowHeaders,
            String errorMessage
    ) {
        errorEventProducer.send(
                InstanceFlowErrorEventProducerRecord
                        .builder()
                        .topicNameParameters(errorEventTopicNameParameters)
                        .instanceFlowHeaders(instanceFlowHeaders)
                        .errorCollection(
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

        errorEventProducer.send(
                InstanceFlowErrorEventProducerRecord
                        .builder()
                        .topicNameParameters(errorEventTopicNameParameters)
                        .instanceFlowHeaders(instanceFlowHeaders)
                        .errorCollection(
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
