package no.fintlabs.flyt.gateway.application.archive.resource.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters;

import java.util.Optional;

@AllArgsConstructor
public class EntityPipeline {

    @Getter
    private EntityTopicNameParameters topicNameParameters;
    @Getter
    private String fintEndpoint;
    @Getter
    private String selfLinkKeyFilter;
    @Getter
    private String fullClassName;

    private SubEntityPipeline subEntityPipeline;

    public Optional<SubEntityPipeline> getSubEntityPipeline() {
        return Optional.ofNullable(subEntityPipeline);
    }
}
