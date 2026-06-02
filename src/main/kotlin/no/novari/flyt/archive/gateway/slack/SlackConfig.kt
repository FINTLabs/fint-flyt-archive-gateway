package no.novari.flyt.archive.gateway.slack

import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.ClientHttpRequestFactory
import org.springframework.web.client.RestClient
import java.time.Duration
import java.util.concurrent.Executor
import java.util.concurrent.Executors

@Configuration
class SlackConfig {
    @Bean("slackClientHttpRequestFactory")
    fun slackClientHttpRequestFactory(): ClientHttpRequestFactory =
        ClientHttpRequestFactoryBuilder
            .jdk()
            .build(
                ClientHttpRequestFactorySettings
                    .defaults()
                    .withConnectTimeout(SLACK_CONNECT_TIMEOUT)
                    .withReadTimeout(SLACK_READ_TIMEOUT),
            )

    @Bean("slackRestClient")
    fun slackRestClient(
        restClientBuilder: RestClient.Builder,
        slackClientHttpRequestFactory: ClientHttpRequestFactory,
    ): RestClient =
        restClientBuilder
            .requestFactory(slackClientHttpRequestFactory)
            .build()

    @Bean("slackAlertExecutor")
    fun slackAlertExecutor(): Executor = Executors.newVirtualThreadPerTaskExecutor()

    companion object {
        private val SLACK_CONNECT_TIMEOUT: Duration = Duration.ofSeconds(5)
        private val SLACK_READ_TIMEOUT: Duration = Duration.ofSeconds(10)
    }
}
