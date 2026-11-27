package no.novari.flyt.archive.gateway.resource.configuration

import no.novari.fint.model.resource.FintLinks
import no.novari.flyt.archive.gateway.links.ResourceLinkUtil
import no.novari.flyt.cache.FlytCache
import no.novari.kafka.starter.topic.KafkaTopics
import org.springframework.stereotype.Service

@Service
class FintLinksResourcePipelineFactory(
    private val topics: KafkaTopics,
) {
    fun <T : FintLinks> createResourcePipeLine(
        resourceClass: Class<T>,
        cache: FlytCache<String, T>,
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
                    .topic(topics.entity(resourceReferencePath.joinToString("-")))
                    .build(),
            )
        }

        return resourcePipelineBuilder.build()
    }
}
