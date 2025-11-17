package no.novari.flyt.archive.gateway;

import lombok.extern.slf4j.Slf4j;
import no.novari.flyt.archive.gateway.slack.SlackAlertService;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@Slf4j
public class WebUtilErrorHandler {

    private final SlackAlertService slackAlertService;

    public WebUtilErrorHandler(SlackAlertService slackAlertService) {
        this.slackAlertService = slackAlertService;
    }

    public void logAndSendError(Throwable e) {
        String errorMessage;
        if (e instanceof WebClientResponseException ex) {
            errorMessage = ex.getResponseBodyAsString();
            log.error("{} body={}", ex, errorMessage);
        } else {
            errorMessage = e.toString();
            log.error(errorMessage);
        }
        slackAlertService.sendMessage(errorMessage);
    }
}
