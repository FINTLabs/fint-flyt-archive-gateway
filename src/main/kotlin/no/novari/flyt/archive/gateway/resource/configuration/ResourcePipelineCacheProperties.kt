package no.novari.flyt.archive.gateway.resource.configuration

import no.novari.flyt.cache.FlytCache
import java.util.function.Function

data class ResourcePipelineCacheProperties<T : Any>(
    val createKeys: Function<T, List<String>>,
    val cache: FlytCache<String, T>,
) {
    companion object {
        @JvmStatic
        fun <T : Any> builder() = Builder<T>()
    }

    class Builder<T : Any> {
        private var createKeys: Function<T, List<String>>? = null
        private var cache: FlytCache<String, T>? = null

        fun createKeys(createKeys: Function<T, List<String>>) = apply { this.createKeys = createKeys }

        fun cache(cache: FlytCache<String, T>) = apply { this.cache = cache }

        fun build() =
            ResourcePipelineCacheProperties(
                createKeys = requireNotNull(createKeys) { "createKeys is required" },
                cache = requireNotNull(cache) { "cache is required" },
            )
    }
}
