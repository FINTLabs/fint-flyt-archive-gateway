package no.fintlabs.flyt.gateway.application.archive.resource.configuration;


import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EntityPipelineFactory {

    @Value("${fint.flyt.gateway.application.archive.resource.base-path}")
    private String basePath;

    public EntityPipeline create(EntityPipelineConfiguration configuration) {

        EntityTopicNameParameters topicNameParameters =
                EntityTopicNameParameters.builder()
                        .resource(
                                configuration.getClassPath() + "-" +
                                        configuration
                                                .getResourceReference()
                                                .toLowerCase()
                                                .replace("resource", "")
                                                .replace(".", "-"))
                        .build();

        String effectiveClassPath = StringUtils.isNotEmpty(configuration.getEndpointClassPath())
                ? configuration.getEndpointClassPath()
                : configuration.getClassPath();

        String fintEndpoint = StringUtils.isNotEmpty(configuration.getFintEndpoint())
                ? configuration.getFintEndpoint()
                : (
                effectiveClassPath + "." +
                        configuration
                                .getResourceReference()
                                .toLowerCase()
                                .replace("resource", ""))
                .replace(".", "/");

        String selfLinkKeyFilter = StringUtils.isNotEmpty(configuration.getKafkaLinkKeyFilter())
                ? configuration.getKafkaLinkKeyFilter()
                : "systemid";

        String fullClassName = basePath + "." + configuration.getClassPath() + "." + configuration.getResourceReference();

        return new EntityPipeline(
                topicNameParameters,
                fintEndpoint,
                selfLinkKeyFilter,
                fullClassName,
                configuration.getSubEntityPipelineConfiguration() != null
                        ? createSubEntityPipeline(configuration.getResourceReference(), configuration.getSubEntityPipelineConfiguration())
                        : null
        );
    }

    private SubEntityPipeline createSubEntityPipeline(String resourceReference, SubEntityPipelineConfiguration subEntityPipelineConfiguration) {
        return new SubEntityPipeline(
                EntityTopicNameParameters.builder()
                        .resource(resourceReference + "-" + subEntityPipelineConfiguration.getReference())
                        .build(),
                subEntityPipelineConfiguration.getReference(),
                subEntityPipelineConfiguration.getKeySuffixFilter()
        );
    }

}
