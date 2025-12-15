package no.novari.flyt.archive.gateway.resource.configuration;

import lombok.Builder;
import lombok.Getter;

import java.util.Optional;

@Builder
public class ResourcePipeline<T> {
    @Getter
    private Class<T> resourceClass;
    @Getter
    private String urlResourcePath;

    private ResourcePipelineCacheProperties<T> cacheProperties;
    private ResourcePipelineKafkaProperties<T> kafkaProperties;

    public Optional<ResourcePipelineCacheProperties<T>> getCacheProperties() {
        return Optional.ofNullable(cacheProperties);
    }

    public Optional<ResourcePipelineKafkaProperties<T>> getKafkaProperties() {
        return Optional.ofNullable(kafkaProperties);
    }
}
