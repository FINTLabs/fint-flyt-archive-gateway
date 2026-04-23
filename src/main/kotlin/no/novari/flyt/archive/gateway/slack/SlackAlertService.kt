package no.novari.flyt.archive.gateway.slack

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Service
class SlackAlertService(
    @param:Qualifier("slackWebClient")
    private val webClient: WebClient,
) {
    @Value("\${fint.org-id}")
    private lateinit var orgId: String

    @Value("\${fint.application-id}")
    private lateinit var applicationId: String

    @Value("\${slack.webhook.url}")
    private lateinit var slackWebhookUrl: String

    fun sendMessage(message: String): Mono<Void> {
        val payload = mapOf("text" to formatMessageWithPrefix(message))

        return webClient
            .post()
            .uri(slackWebhookUrl)
            .bodyValue(payload)
            .retrieve()
            .bodyToMono(Void::class.java)
    }

    private fun formatMessageWithPrefix(message: String): String = "$orgId-$applicationId-$message"
}
