package no.novari.flyt.archive.gateway

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder
import org.springframework.boot.http.client.HttpClientSettings
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.ClientHttpRequestFactory
import java.time.Duration

@Configuration
@ConfigurationProperties("novari.flyt.archive.gateway.http-client")
class HttpClientConfiguration {
    var connectTimeout: Duration? = null
    var defaultResponseTimeout: Duration? = null

    @Bean
    fun clientHttpRequestFactory(): ClientHttpRequestFactory =
        ClientHttpRequestFactoryBuilder
            .jdk()
            .build(
                HttpClientSettings
                    .defaults()
                    .withConnectTimeout(requireNotNull(connectTimeout))
                    .withReadTimeout(requireNotNull(defaultResponseTimeout)),
            )
}
