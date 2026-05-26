package no.novari.flyt.archive.gateway

import no.novari.flyt.archive.gateway.slack.SlackAlertService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientResponseException

@Service
class WebUtilErrorHandler(
    private val slackAlertService: SlackAlertService,
) {
    fun logAndSendError(error: Throwable) {
        val errorMessage =
            if (error is RestClientResponseException) {
                val responseBody = error.responseBodyAsString
                log.error("{} body={}", error, responseBody)
                responseBody
            } else {
                log.error(error.toString(), error)
                error.toString()
            }

        try {
            slackAlertService.sendMessage(errorMessage)
        } catch (sendError: Throwable) {
            log.warn("Failed to send Slack alert", sendError)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(WebUtilErrorHandler::class.java)
    }
}
