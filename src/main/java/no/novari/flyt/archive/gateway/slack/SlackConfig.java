package no.novari.flyt.archive.gateway.slack;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class SlackConfig {

    @Bean("slackWebClient")
    public WebClient slackWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder.build();
    }
}
