package no.novari.flyt.archive.gateway.dispatch.web.flytfile;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "novari.flyt.archive.gateway.client.fint-flyt-file")
public class FlytFileWebClientConfiguration {

    private String baseUrl;

    @Bean
    public ReactiveOAuth2AuthorizedClientManager fileAuthorizedClientManager(
            ReactiveClientRegistrationRepository clientRegistrationRepository,
            ReactiveOAuth2AuthorizedClientService authorizedClientService
    ) {
        AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager authorizedClientManager =
                new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(
                        clientRegistrationRepository,
                        authorizedClientService
                );
        authorizedClientManager.setAuthorizedClientProvider(
                ReactiveOAuth2AuthorizedClientProviderBuilder
                        .builder()
                        .clientCredentials()
                        .refreshToken()
                        .build()
        );
        return authorizedClientManager;
    }

    @Bean
    public WebClient fileWebClient(
            @Qualifier("fileAuthorizedClientManager") Optional<ReactiveOAuth2AuthorizedClientManager> authorizedClientManager,
            ClientHttpConnector clientHttpConnector
    ) {
        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(-1))
                .build();

        WebClient.Builder webClientBuilder = WebClient.builder();

        authorizedClientManager.ifPresent(presentAuthorizedClientManager -> {
            ServerOAuth2AuthorizedClientExchangeFilterFunction authorizedClientExchangeFilterFunction =
                    new ServerOAuth2AuthorizedClientExchangeFilterFunction(presentAuthorizedClientManager);
            authorizedClientExchangeFilterFunction.setDefaultClientRegistrationId("file-service");
            webClientBuilder.filter(authorizedClientExchangeFilterFunction);
        });

        return webClientBuilder
                .clientConnector(clientHttpConnector)
                .baseUrl(baseUrl + "/api/intern-klient/filer")
                .exchangeStrategies(exchangeStrategies)
                .build();
    }

}
