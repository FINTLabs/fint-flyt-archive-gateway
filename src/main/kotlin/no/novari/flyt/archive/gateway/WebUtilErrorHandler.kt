package no.novari.flyt.archive.gateway

import no.novari.flyt.archive.gateway.slack.SlackAlertService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.scheduler.Schedulers

@Service
class WebUtilErrorHandler(
    private val slackAlertService: SlackAlertService,
) {
    fun logAndSendError(error: Throwable) {
        val errorMessage =
            if (error is WebClientResponseException) {
                val responseBody = error.responseBodyAsString
                log.error("{} body={}", error, responseBody)
                responseBody
            } else {
                log.error(error.toString(), error)
                error.toString()
            }

        slackAlertService
            .sendMessage(errorMessage)
            .subscribeOn(Schedulers.boundedElastic())
            .doOnError { sendError -> log.warn("Failed to send Slack alert", sendError) }
            .subscribe()
    }

    companion object {
        private val log = LoggerFactory.getLogger(WebUtilErrorHandler::class.java)
    }
}
