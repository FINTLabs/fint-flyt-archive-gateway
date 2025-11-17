package no.novari.flyt.archive.gateway;

import io.netty.channel.ChannelOption;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties("novari.flyt.archive.gateway.web-client")
@Configuration
public class WebClientConfiguration {

    private Duration connectionMaxLifeTime;
    private Duration connectionMaxIdleTime;
    private Duration connectTimeout;
    private Duration defaultResponseTimeout;

    @Bean
    public ClientHttpConnector clientHttpConnector() {
        return new ReactorClientHttpConnector(HttpClient.create(
                        ConnectionProvider
                                .builder("laidback")
                                .maxLifeTime(connectionMaxLifeTime)
                                .maxIdleTime(connectionMaxIdleTime)
                                .build())
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) connectTimeout.toMillis())
                .responseTimeout(defaultResponseTimeout)
        );
    }

}
