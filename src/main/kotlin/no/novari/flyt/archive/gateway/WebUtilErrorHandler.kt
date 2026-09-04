package no.novari.flyt.archive.gateway

import io.github.oshai.kotlinlogging.KotlinLogging
import no.novari.flyt.archive.gateway.slack.SlackAlertService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientResponseException
import java.util.concurrent.Executor

@Service
class WebUtilErrorHandler(
    private val slackAlertService: SlackAlertService,
    @param:Qualifier("slackAlertExecutor")
    private val slackAlertExecutor: Executor,
) {
    fun logAndSendError(error: Throwable) {
        val errorMessage =
            if (error is RestClientResponseException) {
                val responseBody = error.responseBodyAsString
                log.atError {
                    message = "{} body={}"
                    arguments = arrayOf(error, responseBody)
                }
                responseBody
            } else {
                log.atError {
                    message = error.toString()
                    cause = error
                }
                error.toString()
            }

        slackAlertExecutor.execute {
            try {
                slackAlertService.sendMessage(errorMessage)
            } catch (sendError: Throwable) {
                log.atWarn {
                    message = "Failed to send Slack alert"
                    cause = sendError
                }
            }
        }
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}
