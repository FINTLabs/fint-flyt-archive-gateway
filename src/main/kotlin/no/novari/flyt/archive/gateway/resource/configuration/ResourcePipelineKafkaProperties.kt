package no.novari.flyt.archive.gateway.resource.configuration

import no.novari.kafka.starter.topic.EntityTopicName
import java.util.function.Function

data class ResourcePipelineKafkaProperties<T>(
    val createKafkaKey: Function<T, String>,
    val topic: EntityTopicName,
)
