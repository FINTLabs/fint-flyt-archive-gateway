package no.novari.flyt.archive.gateway.resource.configuration

import no.novari.kafka.topic.name.EntityTopicNameParameters
import java.util.function.Function

data class ResourcePipelineKafkaProperties<T>(
    val createKafkaKey: Function<T, String>,
    val topicNameParameters: EntityTopicNameParameters,
) {
    companion object {
        @JvmStatic
        fun <T> builder() = Builder<T>()
    }

    class Builder<T> {
        private var createKafkaKey: Function<T, String>? = null
        private var topicNameParameters: EntityTopicNameParameters? = null

        fun createKafkaKey(createKafkaKey: Function<T, String>) = apply { this.createKafkaKey = createKafkaKey }

        fun topicNameParameters(topicNameParameters: EntityTopicNameParameters) =
            apply {
                this.topicNameParameters = topicNameParameters
            }

        fun build() =
            ResourcePipelineKafkaProperties(
                createKafkaKey = requireNotNull(createKafkaKey) { "createKafkaKey is required" },
                topicNameParameters = requireNotNull(topicNameParameters) { "topicNameParameters is required" },
            )
    }
}
