package no.novari.flyt.archive.gateway

import io.netty.channel.ChannelOption
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ClientHttpConnector
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import java.time.Duration

@Configuration
@ConfigurationProperties("novari.flyt.archive.gateway.web-client")
class WebClientConfiguration {
    var connectionMaxLifeTime: Duration? = null
    var connectionMaxIdleTime: Duration? = null
    var connectTimeout: Duration? = null
    var defaultResponseTimeout: Duration? = null

    @Bean
    fun clientHttpConnector(): ClientHttpConnector =
        ReactorClientHttpConnector(
            HttpClient
                .create(
                    ConnectionProvider
                        .builder("laidback")
                        .maxLifeTime(requireNotNull(connectionMaxLifeTime))
                        .maxIdleTime(requireNotNull(connectionMaxIdleTime))
                        .build(),
                ).option(ChannelOption.CONNECT_TIMEOUT_MILLIS, requireNotNull(connectTimeout).toMillis().toInt())
                .responseTimeout(requireNotNull(defaultResponseTimeout)),
        )
}
