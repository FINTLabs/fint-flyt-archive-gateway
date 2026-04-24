package no.novari.flyt.archive.gateway.resource.configuration

import no.novari.cache.FintCache
import java.util.function.Function

data class ResourcePipelineCacheProperties<T>(
    val createKeys: Function<T, List<String>>,
    val cache: FintCache<String, T>,
) {
    companion object {
        @JvmStatic
        fun <T> builder() = Builder<T>()
    }

    class Builder<T> {
        private var createKeys: Function<T, List<String>>? = null
        private var cache: FintCache<String, T>? = null

        fun createKeys(createKeys: Function<T, List<String>>) = apply { this.createKeys = createKeys }

        fun cache(cache: FintCache<String, T>) = apply { this.cache = cache }

        fun build() =
            ResourcePipelineCacheProperties(
                createKeys = requireNotNull(createKeys) { "createKeys is required" },
                cache = requireNotNull(cache) { "cache is required" },
            )
    }
}
