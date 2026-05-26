package no.novari.flyt.archive.gateway.slack

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class SlackConfig {
    @Bean("slackRestClient")
    fun slackRestClient(restClientBuilder: RestClient.Builder): RestClient = restClientBuilder.build()
}
