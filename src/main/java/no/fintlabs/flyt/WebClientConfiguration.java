package no.fintlabs.flyt;

import io.netty.channel.ChannelOption;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

@Configuration
public class WebClientConfiguration {

    @Bean
    public ClientHttpConnector clientHttpConnector() {

        ConnectionProvider connectionProvider = ConnectionProvider.builder("myConnectionProvider")
                // Max number of connections in the pool
                .maxConnections(200)
                .maxLifeTime(Duration.ofMinutes(30))
                .maxIdleTime(Duration.ofMinutes(2))
                // How many pending requests can wait for a connection
//                .pendingAcquireMaxCount(1000)
                // How long to wait for a connection before timing out
//                .pendingAcquireTimeout(Duration.ofSeconds(45))
                .build();

        HttpClient httpClient = HttpClient.create(connectionProvider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 120000)
                .responseTimeout(Duration.ofSeconds(130));

        return new ReactorClientHttpConnector(httpClient);
    }

}
