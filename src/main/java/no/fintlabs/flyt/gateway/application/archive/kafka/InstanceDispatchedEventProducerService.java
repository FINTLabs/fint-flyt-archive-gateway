package no.fintlabs.flyt.gateway.application.archive.kafka;

import no.fintlabs.flyt.kafka.event.InstanceFlowEventProducer;
import no.fintlabs.flyt.kafka.event.InstanceFlowEventProducerFactory;
import no.fintlabs.flyt.kafka.event.InstanceFlowEventProducerRecord;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;
import no.fintlabs.kafka.event.topic.EventTopicNameParameters;
import no.fintlabs.kafka.event.topic.EventTopicService;
import org.springframework.stereotype.Service;

@Service
public class InstanceDispatchedEventProducerService {

    private final InstanceFlowEventProducer<Object> eventProducer;
    private final EventTopicNameParameters eventTopicNameParameters;

    public InstanceDispatchedEventProducerService(
            InstanceFlowEventProducerFactory eventProducerFactory,
            EventTopicService eventTopicService,
            KafkaTopicProperties kafkaTopicProperties
    ) {
        this.eventProducer = eventProducerFactory.createProducer(Object.class);
        eventTopicNameParameters = EventTopicNameParameters
                .builder()
                .eventName("instance-dispatched")
                .build();
        eventTopicService.ensureTopic(eventTopicNameParameters, kafkaTopicProperties.getInstanceProcessingEventsRetentionTimeMs());
    }

    public void publish(InstanceFlowHeaders instanceFlowHeaders) {
        eventProducer.send(
                InstanceFlowEventProducerRecord
                        .builder()
                        .topicNameParameters(eventTopicNameParameters)
                        .instanceFlowHeaders(instanceFlowHeaders)
                        .value(null)
                        .build()
        );
    }

}
