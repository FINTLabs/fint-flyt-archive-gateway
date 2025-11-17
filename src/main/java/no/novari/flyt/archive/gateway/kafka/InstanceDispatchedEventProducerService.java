package no.novari.flyt.archive.gateway.kafka;

import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeaders;
import no.novari.flyt.kafka.instanceflow.producing.InstanceFlowProducerRecord;
import no.novari.flyt.kafka.instanceflow.producing.InstanceFlowTemplate;
import no.novari.flyt.kafka.instanceflow.producing.InstanceFlowTemplateFactory;
import no.novari.kafka.topic.EventTopicService;
import no.novari.kafka.topic.configuration.EventCleanupFrequency;
import no.novari.kafka.topic.configuration.EventTopicConfiguration;
import no.novari.kafka.topic.name.EventTopicNameParameters;
import no.novari.kafka.topic.name.TopicNamePrefixParameters;
import org.springframework.stereotype.Service;

@Service
public class InstanceDispatchedEventProducerService {

    private final EventTopicNameParameters eventTopicNameParameters;
    private final InstanceFlowTemplate<Object> instanceFlowtemplate;

    private static final int PARTITIONS = 1;

    public InstanceDispatchedEventProducerService(
            InstanceFlowTemplateFactory instanceFlowTemplateFactory,
            EventTopicService eventTopicService,
            KafkaTopicProperties kafkaTopicProperties
    ) {
        instanceFlowtemplate = instanceFlowTemplateFactory.createTemplate(Object.class);

        eventTopicNameParameters = EventTopicNameParameters
                .builder()
                .topicNamePrefixParameters(TopicNamePrefixParameters
                        .builder()
                        .orgIdApplicationDefault()
                        .domainContextApplicationDefault()
                        .build()
                )
                .eventName("instance-dispatched")
                .build();

        eventTopicService.createOrModifyTopic(eventTopicNameParameters,
                EventTopicConfiguration
                        .builder()
                        .partitions(PARTITIONS)
                        .retentionTime(kafkaTopicProperties.getInstanceProcessingEventsRetentionTime())
                        .cleanupFrequency(EventCleanupFrequency.NORMAL)
                        .build()
        );
    }

    public void publish(InstanceFlowHeaders instanceFlowHeaders) {
        instanceFlowtemplate.send(
                InstanceFlowProducerRecord
                        .builder()
                        // TODO: No key??
                        .key("???")
                        .instanceFlowHeaders(instanceFlowHeaders)
                        .topicNameParameters(eventTopicNameParameters)
                        .value(null)
                        .build()
        );
    }

}
