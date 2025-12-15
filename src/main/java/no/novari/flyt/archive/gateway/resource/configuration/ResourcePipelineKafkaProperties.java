package no.novari.flyt.archive.gateway.resource.configuration;

import lombok.Builder;
import lombok.Getter;
import no.novari.kafka.topic.name.EntityTopicNameParameters;

import java.util.function.Function;

@Builder
@Getter
public class ResourcePipelineKafkaProperties<T> {
    private Function<T, String> createKafkaKey;
    private EntityTopicNameParameters topicNameParameters;
}
