package no.novari.flyt.archive.gateway.slack

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

@Service
class SlackAlertService(
    @param:Qualifier("slackRestClient")
    private val restClient: RestClient,
) {
    @Value("\${fint.org-id}")
    private lateinit var orgId: String

    @Value("\${fint.application-id}")
    private lateinit var applicationId: String

    @Value("\${slack.webhook.url}")
    private lateinit var slackWebhookUrl: String

    fun sendMessage(message: String) {
        val payload = mapOf("text" to formatMessageWithPrefix(message))

        restClient
            .post()
            .uri(slackWebhookUrl)
            .body(payload)
            .retrieve()
            .toBodilessEntity()
    }

    private fun formatMessageWithPrefix(message: String): String = "$orgId-$applicationId-$message"
}
