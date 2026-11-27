package no.novari.flyt.archive.gateway.resource.configuration

data class ResourcePipeline<T : Any>(
    val resourceClass: Class<T>,
    val urlResourcePath: String,
    val cacheProperties: ResourcePipelineCacheProperties<T>? = null,
    val kafkaProperties: ResourcePipelineKafkaProperties<T>? = null,
) {
    companion object {
        @JvmStatic
        fun <T : Any> builder() = Builder<T>()
    }

    class Builder<T : Any> {
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
