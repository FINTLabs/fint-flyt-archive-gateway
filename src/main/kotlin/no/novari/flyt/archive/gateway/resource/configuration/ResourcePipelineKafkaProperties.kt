package no.novari.flyt.archive.gateway.resource.configuration

import no.novari.kafka.starter.topic.EntityTopicName
import java.util.function.Function

data class ResourcePipelineKafkaProperties<T>(
    val createKafkaKey: Function<T, String>,
    val topic: EntityTopicName,
) {
    companion object {
        @JvmStatic
        fun <T> builder() = Builder<T>()
    }

    class Builder<T> {
        private var createKafkaKey: Function<T, String>? = null
        private var topic: EntityTopicName? = null

        fun createKafkaKey(createKafkaKey: Function<T, String>) = apply { this.createKafkaKey = createKafkaKey }

        fun topic(topic: EntityTopicName) = apply { this.topic = topic }

        fun build() =
            ResourcePipelineKafkaProperties(
                createKafkaKey = requireNotNull(createKafkaKey) { "createKafkaKey is required" },
                topic = requireNotNull(topic) { "topic is required" },
            )
    }
}
