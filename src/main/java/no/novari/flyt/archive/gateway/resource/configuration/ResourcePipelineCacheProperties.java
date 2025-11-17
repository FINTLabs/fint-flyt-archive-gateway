package no.novari.flyt.archive.gateway.resource.configuration;

import lombok.Builder;
import lombok.Getter;
import no.novari.cache.FintCache;

import java.util.List;
import java.util.function.Function;

@Builder
@Getter
public class ResourcePipelineCacheProperties<T> {
    private Function<T, List<String>> createKeys;
    private FintCache<String, T> cache;
}
