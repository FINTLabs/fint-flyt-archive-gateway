package no.novari.flyt.archive.gateway.resource.configuration

import no.novari.flyt.cache.FlytCache
import java.util.function.Function

data class ResourcePipelineCacheProperties<T : Any>(
    val createKeys: Function<T, List<String>>,
    val cache: FlytCache<String, T>,
)
