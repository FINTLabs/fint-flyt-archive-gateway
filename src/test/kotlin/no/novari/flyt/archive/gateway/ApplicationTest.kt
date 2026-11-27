package no.novari.flyt.archive.gateway

import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.common.config.TopicConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.config.KafkaListenerEndpointRegistry
import org.springframework.kafka.core.KafkaAdmin
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.test.util.ReflectionTestUtils
import java.time.Duration

@SpringBootTest(
    properties = [
        "fint.org-id=fintlabs.no",
        "novari.kafka.topic.org-id=fintlabs-no",
        "spring.kafka.listener.auto-startup=false",
        "spring.kafka.admin.auto-create=false",
        "novari.flyt.archive.gateway.client.fint-archive.base-url=https://beta.felleskomponent.no",
        "novari.flyt.archive.gateway.resource.publishing.pull.initial-delay=1h",
        "fint.core.oauth2.username=user",
        "fint.core.oauth2.password=password",
        "fint.core.oauth2.client-id=fint-client",
        "fint.core.oauth2.client-secret=secret",
        "fint.sso.client-id=file-client",
        "fint.sso.client-secret=secret",
        "fint.flyt.authorization.sso.client-id=internal-client",
        "novari.flyt.security.user.audience=frontend-client",
        "slack.webhook.url=https://example.com/slack",
    ],
)
class ApplicationTest {
    @Autowired
    private lateinit var listeners: KafkaListenerEndpointRegistry

    @Autowired
    private lateinit var resourceTopics: KafkaAdmin.NewTopics

    @Autowired
    private lateinit var clientRegistrations: ClientRegistrationRepository

    @Test
    fun `instance-mapped listener keeps the application group id, topic and poll settings`() {
        val container = listeners.listenerContainers.single()

        assertThat(container.groupId).isEqualTo("fint-flyt-archive-gateway")
        assertThat(container.containerProperties.topics).containsExactly("fintlabs-no.flyt.event.instance-mapped")
        assertThat(container.containerProperties.kafkaConsumerProperties)
            .containsEntry("max.poll.records", "1")
            .containsEntry("max.poll.interval.ms", "1800000")
    }

    @Test
    fun `resource topics keep their names and delete old records`() {
        // NewTopics has no public getter.
        @Suppress("UNCHECKED_CAST")
        val newTopics = ReflectionTestUtils.getField(resourceTopics, "newTopics") as Collection<NewTopic>
        val topics = newTopics.associateBy { it.name() }

        assertThat(topics).hasSize(19)
        assertThat(topics.keys).contains(
            "fintlabs-no.flyt.entity.arkiv-noark-administrativenhet",
            "fintlabs-no.flyt.entity.arkiv-noark-arkivressurs",
            "fintlabs-no.flyt.entity.arkiv-kodeverk-saksstatus",
            "fintlabs-no.flyt.entity.administrasjon-personal-personalressurs",
            "fintlabs-no.flyt.entity.administrasjon-personal-person",
        )
        val retention = Duration.ofDays(5).toMillis().toString()
        topics.values.forEach { topic ->
            assertThat(topic.configs())
                .containsEntry(TopicConfig.CLEANUP_POLICY_CONFIG, "delete,compact")
                .containsEntry(TopicConfig.RETENTION_MS_CONFIG, retention)
                .containsEntry(TopicConfig.DELETE_RETENTION_MS_CONFIG, retention)
        }
    }

    @Test
    fun `FINT archive registration uses the password grant`() {
        val registration = requireNotNull(clientRegistrations.findByRegistrationId("fint"))

        assertThat(registration.authorizationGrantType).isEqualTo(PasswordGrantAuthorizedClientProvider.PASSWORD)
        assertThat(registration.scopes).containsExactly("fint-client")
    }
}
