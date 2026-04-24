package no.novari.flyt.archive.gateway.slack

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class SlackConfig {
    @Bean("slackWebClient")
    fun slackWebClient(webClientBuilder: WebClient.Builder): WebClient = webClientBuilder.build()
}
