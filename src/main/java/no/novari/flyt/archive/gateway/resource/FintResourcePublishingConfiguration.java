package no.novari.flyt.archive.gateway.resource;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import no.novari.flyt.archive.gateway.resource.configuration.ResourcePipeline;
import no.novari.flyt.archive.gateway.resource.configuration.ResourcePublishingConfigurationProperties;
import no.novari.flyt.archive.gateway.resource.web.FintArchiveResourceClient;
import no.novari.kafka.producing.ParameterizedProducerRecord;
import no.novari.kafka.producing.ParameterizedTemplate;
import no.novari.kafka.producing.ParameterizedTemplateFactory;
import no.novari.kafka.topic.EntityTopicService;
import no.novari.kafka.topic.configuration.EntityCleanupFrequency;
import no.novari.kafka.topic.configuration.EntityTopicConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.IntervalTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.web.reactive.function.client.WebClientException;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Configuration
public class FintResourcePublishingConfiguration implements SchedulingConfigurer {

    private final Random randomSeededByOrgAndApplicationId;
    private final ResourcePublishingConfigurationProperties resourcePublishingConfigurationProperties;
    private final List<ResourcePipeline<?>> resourcePipelines;
    private final EntityTopicService entityTopicService;
    private final ParameterizedTemplate<Object> parameterizedTemplate;
    private final FintArchiveResourceClient fintArchiveResourceClient;
    private final Map<String, Long> lastUpdatedTimestampForPulledResourcesPerResourcePath;

    private static final int PARTITIONS = 1;

    public FintResourcePublishingConfiguration(
            @Value("${fint.org-id}") String orgId,
            @Value("${fint.application-id}") String applicationId,
            ResourcePublishingConfigurationProperties resourcePublishingConfigurationProperties,
            List<ResourcePipeline<?>> resourcePipelines,
            EntityTopicService entityTopicService,
            ParameterizedTemplateFactory parameterizedTemplateFactory,
            FintArchiveResourceClient fintArchiveResourceClient) {
        this.randomSeededByOrgAndApplicationId = new Random(Objects.hash(orgId, applicationId));
        this.resourcePublishingConfigurationProperties = resourcePublishingConfigurationProperties;
        this.resourcePipelines = resourcePipelines;
        this.entityTopicService = entityTopicService;
        this.parameterizedTemplate = parameterizedTemplateFactory.createTemplate(Object.class);
        this.fintArchiveResourceClient = fintArchiveResourceClient;
        this.lastUpdatedTimestampForPulledResourcesPerResourcePath = new ConcurrentHashMap<>(resourcePipelines.size());
        this.ensureTopics();
    }

    private void ensureTopics() {
        Duration resourceTopicRetentionTime = Duration.ofDays(1)
                .plus(resourcePublishingConfigurationProperties.getReset().getResourceTopicRetentionTimeOffset());

        resourcePipelines.forEach(
                resourcePipeline -> resourcePipeline.getKafkaProperties().ifPresent(
                        kafkaProperties -> this.entityTopicService.createOrModifyTopic(
                                kafkaProperties.getTopicNameParameters(),
                                EntityTopicConfiguration
                                        .stepBuilder()
                                        .partitions(PARTITIONS)
                                        .lastValueRetentionTime(resourceTopicRetentionTime)
                                        .nullValueRetentionTime(resourceTopicRetentionTime)
                                        .cleanupFrequency(EntityCleanupFrequency.NORMAL)
                                        .build()
                        )
                ));
    }

    @Override
    public void configureTasks(@NonNull ScheduledTaskRegistrar taskRegistrar) {
        LocalTime timeOfDayToReset = getTimeOfDayToReset();
        CronTrigger cronTrigger = createCronTrigger(timeOfDayToReset);
        taskRegistrar.addCronTask(
                new CronTask(
                        () -> {
                            lastUpdatedTimestampForPulledResourcesPerResourcePath.clear();
                            log.info("Reset last updated timestamp");
                        },
                        cronTrigger
                )
        );
        log.info("Scheduled reset of last updated timestamp at {}", timeOfDayToReset);

        taskRegistrar.addFixedDelayTask(
                new IntervalTask(
                        this::pullAllUpdatedResources,
                        resourcePublishingConfigurationProperties.getPull().getFixedDelay(),
                        resourcePublishingConfigurationProperties.getPull().getInitialDelay()
                )
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

    private LocalTime getTimeOfDayToReset() {
        LocalTime from = resourcePublishingConfigurationProperties.getReset().getFromTimeOfDay();
        LocalTime to = resourcePublishingConfigurationProperties.getReset().getToTimeOfDay();
        if (from.equals(to)) {
            return from;
        }
        Duration duration = from.isBefore(to)
                ? Duration.between(from, to)
                : Duration.between(from, to).plusHours(24);
        return from
                .plusSeconds(
                        randomSeededByOrgAndApplicationId.nextLong(
                                1,
                                duration.toSeconds()
                        )
                );
    }

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
                kafkaProperties -> parameterizedTemplate.send(
                        ParameterizedProducerRecord
                                .builder()
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
