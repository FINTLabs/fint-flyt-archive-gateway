package no.novari.flyt.archive.gateway.resource.configuration

import no.novari.cache.FintCache
import no.novari.fint.model.resource.FintLinks
import no.novari.flyt.archive.gateway.links.ResourceLinkUtil
import no.novari.kafka.topic.name.EntityTopicNameParameters
import no.novari.kafka.topic.name.TopicNamePrefixParameters
import org.springframework.stereotype.Service

@Service
class FintLinksResourcePipelineFactory {
    fun <T : FintLinks> createResourcePipeLine(
        resourceClass: Class<T>,
        cache: FintCache<String, T>,
        resourceReferencePath: List<String>,
        publishOnKafka: Boolean,
    ): ResourcePipeline<T> {
        val resourcePipelineBuilder =
            ResourcePipeline
                .builder<T>()
                .resourceClass(resourceClass)
                .urlResourcePath(resourceReferencePath.joinToString("/"))
                .cacheProperties(
                    ResourcePipelineCacheProperties
                        .builder<T>()
                        .createKeys(ResourceLinkUtil::getSelfLinks)
                        .cache(cache)
                        .build(),
                )

        if (publishOnKafka) {
            resourcePipelineBuilder.kafkaProperties(
                ResourcePipelineKafkaProperties
                    .builder<T>()
                    .createKafkaKey(ResourceLinkUtil::getFirstSelfLink)
                    .topicNameParameters(
                        EntityTopicNameParameters
                            .builder()
                            .topicNamePrefixParameters(
                                TopicNamePrefixParameters
                                    .stepBuilder()
                                    .orgIdApplicationDefault()
                                    .domainContextApplicationDefault()
                                    .build(),
                            ).resourceName(resourceReferencePath.joinToString("-"))
                            .build(),
                    ).build(),
            )
        }

        return resourcePipelineBuilder.build()
    }
}
