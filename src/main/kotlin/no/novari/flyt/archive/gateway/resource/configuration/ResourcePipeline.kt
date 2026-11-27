package no.novari.flyt.archive.gateway.resource.configuration

data class ResourcePipeline<T : Any>(
    val resourceClass: Class<T>,
    val urlResourcePath: String,
    val cacheProperties: ResourcePipelineCacheProperties<T>? = null,
    val kafkaProperties: ResourcePipelineKafkaProperties<T>? = null,
)
