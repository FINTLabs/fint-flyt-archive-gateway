package no.novari.flyt.archive.gateway.resource.configuration

import java.util.Optional
import kotlin.jvm.JvmName

data class ResourcePipeline<T>(
    val resourceClass: Class<T>,
    val urlResourcePath: String,
    @get:JvmName("getCachePropertiesOrNull")
    val cacheProperties: ResourcePipelineCacheProperties<T>? = null,
    @get:JvmName("getKafkaPropertiesOrNull")
    val kafkaProperties: ResourcePipelineKafkaProperties<T>? = null,
) {
    fun getCacheProperties(): Optional<ResourcePipelineCacheProperties<T>> = Optional.ofNullable(cacheProperties)

    fun getKafkaProperties(): Optional<ResourcePipelineKafkaProperties<T>> = Optional.ofNullable(kafkaProperties)

    companion object {
        @JvmStatic
        fun <T> builder() = Builder<T>()
    }

    class Builder<T> {
        private var resourceClass: Class<T>? = null
        private var urlResourcePath: String? = null
        private var cacheProperties: ResourcePipelineCacheProperties<T>? = null
        private var kafkaProperties: ResourcePipelineKafkaProperties<T>? = null

        fun resourceClass(resourceClass: Class<T>) = apply { this.resourceClass = resourceClass }

        fun urlResourcePath(urlResourcePath: String) = apply { this.urlResourcePath = urlResourcePath }

        fun cacheProperties(cacheProperties: ResourcePipelineCacheProperties<T>?) =
            apply {
                this.cacheProperties = cacheProperties
            }

        fun kafkaProperties(kafkaProperties: ResourcePipelineKafkaProperties<T>?) =
            apply {
                this.kafkaProperties = kafkaProperties
            }

        fun build() =
            ResourcePipeline(
                resourceClass = requireNotNull(resourceClass) { "resourceClass is required" },
                urlResourcePath = requireNotNull(urlResourcePath) { "urlResourcePath is required" },
                cacheProperties = cacheProperties,
                kafkaProperties = kafkaProperties,
            )
    }
}
