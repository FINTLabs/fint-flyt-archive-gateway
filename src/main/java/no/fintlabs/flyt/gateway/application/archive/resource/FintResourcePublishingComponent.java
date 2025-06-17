package no.fintlabs.flyt.gateway.application.archive.resource;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.flyt.gateway.application.archive.resource.configuration.ResourcePipeline;
import no.fintlabs.flyt.gateway.application.archive.resource.web.FintArchiveResourceClient;
import no.fintlabs.kafka.entity.EntityProducer;
import no.fintlabs.kafka.entity.EntityProducerFactory;
import no.fintlabs.kafka.entity.EntityProducerRecord;
import no.fintlabs.kafka.entity.topic.EntityTopicService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientException;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;


@Slf4j
@Component
public class FintResourcePublishingComponent {

    private final EntityTopicService entityTopicService;
    private final EntityProducer<Object> entityProducer;
    private final FintArchiveResourceClient fintArchiveResourceClient;
    private final List<ResourcePipeline<?>> resourcePipelines;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private final Map<String, Long> sinceTimestamp = new ConcurrentHashMap<>();

    public FintResourcePublishingComponent(
            EntityTopicService entityTopicService,
            EntityProducerFactory entityProducerFactory,
            FintArchiveResourceClient fintArchiveResourceClient,
            List<ResourcePipeline<?>> resourcePipelines,
            @Value("${fint.flyt.gateway.application.archive.resource.publishing.refresh.topic-retention-time-offset-ms}")
            Long refreshTopicRetentionTimeOffsetMs
    ) {
        this.entityTopicService = entityTopicService;
        this.entityProducer = entityProducerFactory.createProducer(Object.class);
        this.fintArchiveResourceClient = fintArchiveResourceClient;
        this.resourcePipelines = resourcePipelines;
        this.ensureTopics(resourcePipelines, refreshTopicRetentionTimeOffsetMs);
    }

    private void ensureTopics(List<ResourcePipeline<?>> resourcePipelines, long topicRetentionTime) {
        resourcePipelines.forEach(
                resourcePipeline -> resourcePipeline.getKafkaProperties().ifPresent(
                        kafkaProperties -> this.entityTopicService.ensureTopic(
                                kafkaProperties.getTopicNameParameters(),
                                topicRetentionTime
                        )
                ));
    }

    @PostConstruct
    private void scheduleResetLastUpdatedTimestamps() {
        ZonedDateTime now = ZonedDateTime.now();

        ZonedDateTime nextMidnight = now.truncatedTo(ChronoUnit.DAYS).plusDays(1);

        long randomOffsetMs = ThreadLocalRandom.current().nextLong(Duration.ofHours(4).toMillis());

        ZonedDateTime scheduledTime = nextMidnight.plus(Duration.ofMillis(randomOffsetMs));

        long initialDelayMs = Duration.between(now, scheduledTime).toMillis();

        log.info("Scheduled resetLastUpdatedTimestamps() to run at {}", scheduledTime);

        scheduler.scheduleAtFixedRate(
                this::resetLastUpdatedTimestamps,
                initialDelayMs,
                Duration.ofDays(1).toMillis(),
                TimeUnit.MILLISECONDS
        );
    }

    private void resetLastUpdatedTimestamps() {
        log.warn("Resetting resource last updated timestamps");
        sinceTimestamp.clear();
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
        try {
            List<T> resources = getUpdatedResources(resourcePipeline.getUrlResourcePath(), resourcePipeline.getResourceClass());
            if (!resources.isEmpty()) {
                resources.forEach(resource -> handleResource(resource, resourcePipeline));
                resourcePipeline.getCacheProperties().ifPresent(
                        cacheProperties -> log.info(
                                "{} entities cached in {}",
                                resources.size(),
                                cacheProperties.getCache().getAlias()
                        )
                );
                resourcePipeline.getKafkaProperties().ifPresent(
                        kafkaProperties -> log.info(
                                "{} entities sent to {}",
                                resources.size(),
                                kafkaProperties.getTopicNameParameters()
                        )
                );
            }
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
        long lastSeen = sinceTimestamp.getOrDefault(urlResourcePath, 0L);

        try {
            Long newLast = fintArchiveResourceClient
                    .getLastUpdated(urlResourcePath)
                    .block();
            if (newLast == null) {
                log.warn("Last‐updated response was null for {}", urlResourcePath);
                return Collections.emptyList();
            }

            List<T> resources = fintArchiveResourceClient
                    .getResourcesSince(urlResourcePath, resourceClass, lastSeen)
                    .collectList()
                    .block();

            sinceTimestamp.put(urlResourcePath, newLast);
            return resources != null ? resources : Collections.emptyList();

        } catch (WebClientException e) {
            log.error("Could not pull entities from url resource path={}", urlResourcePath, e);
            return Collections.emptyList();
        }
    }

}
