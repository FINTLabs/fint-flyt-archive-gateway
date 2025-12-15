package no.novari.flyt.archive.gateway.resource.configuration;

import no.fint.model.resource.FintLinks;
import no.novari.cache.FintCache;
import no.novari.flyt.archive.gateway.links.ResourceLinkUtil;
import no.novari.kafka.topic.name.EntityTopicNameParameters;
import no.novari.kafka.topic.name.TopicNamePrefixParameters;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FintLinksResourcePipelineFactory {
    public <T extends FintLinks> ResourcePipeline<T> createResourcePipeLine(
            Class<T> resourceClass,
            FintCache<String, T> cache,
            List<String> resourceReferencePath,
            boolean publishOnKafka
    ) {
        ResourcePipeline.ResourcePipelineBuilder<T> resourcePipelineBuilder = ResourcePipeline
                .<T>builder()
                .resourceClass(resourceClass)
                .urlResourcePath(String.join("/", resourceReferencePath))
                .cacheProperties(
                        ResourcePipelineCacheProperties
                                .<T>builder()
                                .createKeys(ResourceLinkUtil::getSelfLinks)
                                .cache(cache)
                                .build()
                );

        if (publishOnKafka) {
            resourcePipelineBuilder.kafkaProperties(
                    ResourcePipelineKafkaProperties
                            .<T>builder()
                            .createKafkaKey(ResourceLinkUtil::getFirstSelfLink)
                            .topicNameParameters(
                                    EntityTopicNameParameters
                                            .builder()
                                            .topicNamePrefixParameters(TopicNamePrefixParameters
                                                    .stepBuilder()
                                                    .orgIdApplicationDefault()
                                                    .domainContextApplicationDefault()
                                                    .build()
                                            )
                                            .resourceName(String.join("-", resourceReferencePath))
                                            .build()
                            )
                            .build()
            );
        }

        return resourcePipelineBuilder.build();
    }
}
