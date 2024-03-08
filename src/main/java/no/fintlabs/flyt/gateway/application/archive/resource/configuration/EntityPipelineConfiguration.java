package no.fintlabs.flyt.gateway.application.archive.resource.configuration;

import lombok.Data;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class EntityPipelineConfiguration {
    private String resourceReference;
    private String kafkaTopic;
    private String fintEndpoint;
    private String kafkaLinkKeyFilter;
    private String endpointClassPath;
    private String classPath;
    private SubEntityPipelineConfiguration subEntityPipelineConfiguration;
}
