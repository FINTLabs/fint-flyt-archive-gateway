package no.fintlabs.flyt.gateway.application.archive;

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
@ConfigurationProperties("fint.flyt.gateway.application.archive.web-client")
@Configuration
public class WebClientConfiguration {

    private Long connectionMaxLifeTimeMillis;
    private Long connectionMaxIdleTimeMillis;
    private Integer connectTimeoutMillis;
    private Long defaultResponseTimeoutMillis;

    @Bean
    public ClientHttpConnector clientHttpConnector() {
        return new ReactorClientHttpConnector(HttpClient.create(
                        ConnectionProvider
                                .builder("laidback")
                                .maxLifeTime(Duration.ofMillis(connectionMaxLifeTimeMillis))
                                .maxIdleTime(Duration.ofMillis(connectionMaxIdleTimeMillis))
                                .build())
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMillis)
                .responseTimeout(Duration.ofMillis(defaultResponseTimeoutMillis))
        );
    }

}
