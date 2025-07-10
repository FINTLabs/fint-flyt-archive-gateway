package no.fintlabs.flyt.gateway.application.archive.resource;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.flyt.gateway.application.archive.resource.configuration.ResourcePipeline;
import no.fintlabs.flyt.gateway.application.archive.resource.configuration.ResourcePublishingRefreshConfigurationProperties;
import no.fintlabs.flyt.gateway.application.archive.resource.web.FintArchiveResourceClient;
import no.fintlabs.kafka.entity.EntityProducer;
import no.fintlabs.kafka.entity.EntityProducerFactory;
import no.fintlabs.kafka.entity.EntityProducerRecord;
import no.fintlabs.kafka.entity.topic.EntityTopicService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.web.reactive.function.client.WebClientException;

import java.time.Duration;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
@Configuration
public class FintResourcePublishingConfiguration implements SchedulingConfigurer {

    private final Random randomSeededByOrgAndApplicationId;
    private final ResourcePublishingRefreshConfigurationProperties resourcePublishingRefreshConfigurationProperties;
    private final List<ResourcePipeline<?>> resourcePipelines;
    private final EntityTopicService entityTopicService;
    private final EntityProducer<Object> entityProducer;
    private final FintArchiveResourceClient fintArchiveResourceClient;
    private final Map<String, Long> lastUpdatedTimestampForPulledResourcesPerResourcePath;

    public FintResourcePublishingConfiguration(
            @Value("${fint.org-id}") String orgId,
            @Value("${fint.application-id}") String applicationId,
            ResourcePublishingRefreshConfigurationProperties resourcePublishingRefreshConfigurationProperties,
            List<ResourcePipeline<?>> resourcePipelines,
            EntityTopicService entityTopicService,
            EntityProducerFactory entityProducerFactory,
            FintArchiveResourceClient fintArchiveResourceClient
    ) {
        this.randomSeededByOrgAndApplicationId = new Random(Objects.hash(orgId, applicationId));
        this.resourcePublishingRefreshConfigurationProperties = resourcePublishingRefreshConfigurationProperties;
        this.resourcePipelines = resourcePipelines;
        this.entityTopicService = entityTopicService;
        this.entityProducer = entityProducerFactory.createProducer(Object.class);
        this.fintArchiveResourceClient = fintArchiveResourceClient;
        this.lastUpdatedTimestampForPulledResourcesPerResourcePath = new ConcurrentHashMap<>(resourcePipelines.size());
        this.ensureTopics();
    }

    private void ensureTopics() {
        long resourceTopicRetentionTime = Duration.ofDays(1)
                .plus(resourcePublishingRefreshConfigurationProperties.getResourceTopicRetentionTimeOffset())
                .toMillis();

        resourcePipelines.forEach(
                resourcePipeline -> resourcePipeline.getKafkaProperties().ifPresent(
                        kafkaProperties -> this.entityTopicService.ensureTopic(
                                kafkaProperties.getTopicNameParameters(),
                                resourceTopicRetentionTime
                        )
                ));
    }

    @Override
    public void configureTasks(@NonNull ScheduledTaskRegistrar taskRegistrar) {
        resourcePipelines.forEach(
                resourcePipeline -> {
                    LocalTime refreshTimeOfDay = getRefreshTimeOfDay();
                    CronTrigger cronTrigger = createCronTrigger(refreshTimeOfDay);
                    taskRegistrar.addCronTask(
                            new CronTask(
                                    () -> lastUpdatedTimestampForPulledResourcesPerResourcePath
                                            .remove(resourcePipeline.getUrlResourcePath()),
                                    cronTrigger
                            )
                    );
                }
        );
    }

    private CronTrigger createCronTrigger(LocalTime localTime) {
        return new CronTrigger(
                String.format(
                        "%d %d %d * * ?",
                        localTime.getSecond(),
                        localTime.getMinute(),
                        localTime.getHour()
                )
        );
    }

    private LocalTime getRefreshTimeOfDay() {
        LocalTime from = resourcePublishingRefreshConfigurationProperties.getFromTimeOfDay();
        LocalTime to = resourcePublishingRefreshConfigurationProperties.getToTimeOfDay();
        if (from.equals(to)) {
            return from;
        }
        return from
                .plusSeconds(
                        randomSeededByOrgAndApplicationId.nextLong(
                                1,
                                Duration.between(from, to).toSeconds()
                        )
                );
    }

    @Scheduled(
            initialDelayString = "${fint.flyt.gateway.application.archive.resource.publishing.pull.initial-delay-ms}",
            fixedDelayString = "${fint.flyt.gateway.application.archive.resource.publishing.pull.fixed-delay-ms}")
    private void pullAllUpdatedResources() {
        log.info("Starting pulling resources");
        resourcePipelines.forEach(this::pullUpdatedResources);
        log.info("Completed pulling resources");
    }

    private <T> void pullUpdatedResources(ResourcePipeline<T> resourcePipeline) {
        String resourceUrl = resourcePipeline.getUrlResourcePath();
        try {
            List<T> updatedResources = getUpdatedResources(resourceUrl, resourcePipeline.getResourceClass());

            if (updatedResources.isEmpty()) {
                return;
            }

            updatedResources.forEach(resource -> handleResource(resource, resourcePipeline));
            resourcePipeline.getCacheProperties().ifPresent(
                    cacheProperties -> log.info(
                            "{} entities cached in {}",
                            updatedResources.size(),
                            cacheProperties.getCache().getAlias()
                    )
            );
            resourcePipeline.getKafkaProperties().ifPresent(
                    kafkaProperties -> log.info(
                            "{} entities sent to {}",
                            updatedResources.size(),
                            kafkaProperties.getTopicNameParameters()
                    )
            );

        } catch (Exception e) {
            log.error("An error occurred processing entities", e);
        }
    }

    private <T> void handleResource(
            T resource,
            ResourcePipeline<T> resourcePipeline
    ) {
        resourcePipeline.getCacheProperties().ifPresent(
                cacheProperties -> cacheProperties.getCache().put(
                        cacheProperties.getCreateKeys().apply(resource),
                        resource
                )
        );

        resourcePipeline.getKafkaProperties().ifPresent(
                kafkaProperties -> entityProducer.send(
                        EntityProducerRecord.builder()
                                .topicNameParameters(kafkaProperties.getTopicNameParameters())
                                .key(kafkaProperties.getCreateKafkaKey().apply(resource))
                                .value(resource)
                                .build()
                )
        );

    }

    private <T> List<T> getUpdatedResources(String urlResourcePath, Class<T> resourceClass) {
        try {
            Long lastUpdatedTimestampFromServer = fintArchiveResourceClient
                    .getLastUpdated(urlResourcePath)
                    .block();
            if (lastUpdatedTimestampFromServer == null) {
                log.warn("Last‐updated response was null for {}", urlResourcePath);
                return Collections.emptyList();
            }

            long lastUpdatedTimestampForPulledResources = lastUpdatedTimestampForPulledResourcesPerResourcePath.getOrDefault(urlResourcePath, 0L);
            if (lastUpdatedTimestampFromServer == lastUpdatedTimestampForPulledResources) {
                return Collections.emptyList();
            }

            List<T> resources = fintArchiveResourceClient
                    .getResourcesSince(urlResourcePath, resourceClass, lastUpdatedTimestampForPulledResources)
                    .collectList()
                    .block();

            lastUpdatedTimestampForPulledResourcesPerResourcePath.put(urlResourcePath, lastUpdatedTimestampFromServer);
            return resources == null ? Collections.emptyList() : resources;

        } catch (WebClientException e) {
            log.error("Could not pull entities from url resource path={}", urlResourcePath, e);
            return Collections.emptyList();
        }
    }

}
