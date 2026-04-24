package no.novari.flyt.archive.gateway.resource

import no.novari.flyt.archive.gateway.resource.configuration.ResourcePipeline
import no.novari.flyt.archive.gateway.resource.configuration.ResourcePublishingConfigurationProperties
import no.novari.flyt.archive.gateway.resource.web.FintArchiveResourceClient
import no.novari.kafka.producing.ParameterizedProducerRecord
import no.novari.kafka.producing.ParameterizedTemplate
import no.novari.kafka.producing.ParameterizedTemplateFactory
import no.novari.kafka.topic.EntityTopicService
import no.novari.kafka.topic.configuration.EntityCleanupFrequency
import no.novari.kafka.topic.configuration.EntityTopicConfiguration
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.SchedulingConfigurer
import org.springframework.scheduling.config.CronTask
import org.springframework.scheduling.config.IntervalTask
import org.springframework.scheduling.config.ScheduledTaskRegistrar
import org.springframework.scheduling.support.CronTrigger
import org.springframework.web.reactive.function.client.WebClientException
import java.time.Duration
import java.time.LocalTime
import java.util.Random
import java.util.concurrent.ConcurrentHashMap

@Configuration
class FintResourcePublishingConfiguration(
    @Value("\${fint.org-id}") orgId: String,
    @Value("\${fint.application-id}") applicationId: String,
    private val resourcePublishingConfigurationProperties: ResourcePublishingConfigurationProperties,
    resourcePipelines: List<ResourcePipeline<*>>,
    private val entityTopicService: EntityTopicService,
    parameterizedTemplateFactory: ParameterizedTemplateFactory,
    private val fintArchiveResourceClient: FintArchiveResourceClient,
) : SchedulingConfigurer {
    private val randomSeededByOrgAndApplicationId = Random(31L * orgId.hashCode() + applicationId.hashCode())

    @Suppress("UNCHECKED_CAST")
    private val resourcePipelines: List<ResourcePipeline<Any>> = resourcePipelines.map { it as ResourcePipeline<Any> }
    private val parameterizedTemplate: ParameterizedTemplate<Any> =
        parameterizedTemplateFactory.createTemplate(
            Any::class.java,
        )
    private val lastUpdatedTimestampForPulledResourcesPerResourcePath =
        ConcurrentHashMap<String, Long>(resourcePipelines.size)

    init {
        ensureTopics()
    }

    private fun ensureTopics() {
        val resourceTopicRetentionTime =
            Duration
                .ofDays(1)
                .plus(requireNotNull(resourcePublishingConfigurationProperties.reset?.resourceTopicRetentionTimeOffset))

        resourcePipelines.forEach { resourcePipeline ->
            resourcePipeline.kafkaProperties?.let { kafkaProperties ->
                entityTopicService.createOrModifyTopic(
                    kafkaProperties.topicNameParameters,
                    EntityTopicConfiguration
                        .stepBuilder()
                        .partitions(PARTITIONS)
                        .lastValueRetentionTime(resourceTopicRetentionTime)
                        .nullValueRetentionTime(resourceTopicRetentionTime)
                        .cleanupFrequency(EntityCleanupFrequency.NORMAL)
                        .build(),
                )
            }
        }
    }

    override fun configureTasks(taskRegistrar: ScheduledTaskRegistrar) {
        val timeOfDayToReset = getTimeOfDayToReset()
        val cronTrigger = createCronTrigger(timeOfDayToReset)
        taskRegistrar.addCronTask(
            CronTask(
                {
                    lastUpdatedTimestampForPulledResourcesPerResourcePath.clear()
                    log.info("Reset last updated timestamp")
                },
                cronTrigger,
            ),
        )
        log.info("Scheduled reset of last updated timestamp at {}", timeOfDayToReset)

        taskRegistrar.addFixedDelayTask(
            IntervalTask(
                this::pullAllUpdatedResources,
                requireNotNull(resourcePublishingConfigurationProperties.pull?.fixedDelay),
                requireNotNull(resourcePublishingConfigurationProperties.pull?.initialDelay),
            ),
        )
    }

    private fun createCronTrigger(localTime: LocalTime): CronTrigger =
        CronTrigger("${localTime.second} ${localTime.minute} ${localTime.hour} * * ?")

    private fun getTimeOfDayToReset(): LocalTime {
        val from = requireNotNull(resourcePublishingConfigurationProperties.reset?.fromTimeOfDay)
        val to = requireNotNull(resourcePublishingConfigurationProperties.reset?.toTimeOfDay)
        if (from == to) {
            return from
        }

        val duration =
            if (from.isBefore(to)) {
                Duration.between(from, to)
            } else {
                Duration.between(from, to).plusHours(24)
            }

        return from.plusSeconds(randomSeededByOrgAndApplicationId.nextLong(1, duration.seconds))
    }

    private fun pullAllUpdatedResources() {
        log.info("Starting pulling resources")
        resourcePipelines.forEach(::pullUpdatedResources)
        log.info("Completed pulling resources")
    }

    private fun pullUpdatedResources(resourcePipeline: ResourcePipeline<Any>) {
        val resourceUrl = resourcePipeline.urlResourcePath
        try {
            val updatedResources = getUpdatedResources(resourceUrl, resourcePipeline.resourceClass)
            if (updatedResources.isEmpty()) {
                return
            }

            updatedResources.forEach { resource -> handleResource(resource, resourcePipeline) }
            resourcePipeline.cacheProperties?.let { cacheProperties ->
                log.info("{} entities cached in {}", updatedResources.size, cacheProperties.cache.alias)
            }
            resourcePipeline.kafkaProperties?.let { kafkaProperties ->
                log.info("{} entities sent to {}", updatedResources.size, kafkaProperties.topicNameParameters)
            }
        } catch (error: Exception) {
            log.error("An error occurred processing entities", error)
        }
    }

    private fun handleResource(
        resource: Any,
        resourcePipeline: ResourcePipeline<Any>,
    ) {
        resourcePipeline.cacheProperties?.let { cacheProperties ->
            cacheProperties.cache.put(cacheProperties.createKeys.apply(resource), resource)
        }

        resourcePipeline.kafkaProperties?.let { kafkaProperties ->
            parameterizedTemplate.send(
                ParameterizedProducerRecord
                    .builder<Any>()
                    .topicNameParameters(kafkaProperties.topicNameParameters)
                    .key(kafkaProperties.createKafkaKey.apply(resource))
                    .value(resource)
                    .build(),
            )
        }
    }

    private fun getUpdatedResources(
        urlResourcePath: String,
        resourceClass: Class<Any>,
    ): List<Any> =
        try {
            val lastUpdatedTimestampFromServer = fintArchiveResourceClient.getLastUpdated(urlResourcePath).block()
            if (lastUpdatedTimestampFromServer == null) {
                log.warn("Last-updated response was null for {}", urlResourcePath)
                return emptyList()
            }

            val lastUpdatedTimestampForPulledResources =
                lastUpdatedTimestampForPulledResourcesPerResourcePath.getOrDefault(urlResourcePath, 0L)
            if (lastUpdatedTimestampFromServer == lastUpdatedTimestampForPulledResources) {
                return emptyList()
            }

            val resources =
                fintArchiveResourceClient
                    .getResourcesSince(urlResourcePath, resourceClass, lastUpdatedTimestampForPulledResources)
                    .collectList()
                    .block()
                    .orEmpty()

            lastUpdatedTimestampForPulledResourcesPerResourcePath[urlResourcePath] = lastUpdatedTimestampFromServer
            resources
        } catch (error: WebClientException) {
            log.error("Could not pull entities from url resource path={}", urlResourcePath, error)
            emptyList()
        }

    companion object {
        private val log = LoggerFactory.getLogger(FintResourcePublishingConfiguration::class.java)
        private const val PARTITIONS = 1
    }
}
