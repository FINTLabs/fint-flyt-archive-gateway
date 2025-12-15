package no.novari.flyt.archive.gateway.slack;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Service
public class SlackAlertService {

    @Value("${fint.org-id}")
    private String orgId;

    @Value("${fint.application-id}")
    private String applicationId;

    @Value("${slack.webhook.url}")
    private String slackWebhookUrl;

    private final WebClient webClient;

    public SlackAlertService(
            @Qualifier("slackWebClient") WebClient webClient
    ) {
        this.webClient = webClient;
    }

    public void sendMessage(String message) {
        Map<String, String> payload = new HashMap<>();
        String formattedMessage = formatMessageWithPrefix(message);
        payload.put("text", formattedMessage);

        webClient.post()
                .uri(slackWebhookUrl)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    private String formatMessageWithPrefix(String message) {
        return orgId + "-" + applicationId + "-" + message;
    }
}
