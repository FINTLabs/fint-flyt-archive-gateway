package no.fintlabs.flyt.gateway.application.archive;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "fint.flyt.gateway.application.archive.client.fint-archive")
public class FintArchiveWebClientConfiguration {

    private String baseUrl;
    private String username;
    private String password;
    private String registrationId;

    @Bean
    @ConditionalOnProperty(name = "fint.flyt.gateway.application.archive.client.fint-archive.authorization.enable", havingValue = "true")
    public ReactiveOAuth2AuthorizedClientManager fintArchiveAuthorizedClientManager(
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
                        .password()
                        .refreshToken()
                        .build()
        );
        authorizedClientManager.setContextAttributesMapper(
                authorizeRequest -> Mono.just(Map.of(
                        OAuth2AuthorizationContext.USERNAME_ATTRIBUTE_NAME, username,
                        OAuth2AuthorizationContext.PASSWORD_ATTRIBUTE_NAME, password
                ))
        );
        return authorizedClientManager;
    }

    @Bean
    public WebClient fintWebClient(
            WebClient.Builder webClientBuilder,
            @Qualifier("fintArchiveAuthorizedClientManager") Optional<ReactiveOAuth2AuthorizedClientManager> authorizedClientManager,
            ClientHttpConnector clientHttpConnector
    ) {
        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(-1))
                .build();

        authorizedClientManager.ifPresent(presentAuthorizedClientManager -> {
            ServerOAuth2AuthorizedClientExchangeFilterFunction authorizedClientExchangeFilterFunction =
                    new ServerOAuth2AuthorizedClientExchangeFilterFunction(presentAuthorizedClientManager);
            authorizedClientExchangeFilterFunction.setDefaultClientRegistrationId(registrationId);
            webClientBuilder.filter(authorizedClientExchangeFilterFunction);
        });
        return webClientBuilder
                .clientConnector(clientHttpConnector)
                .exchangeStrategies(exchangeStrategies)
                .baseUrl(baseUrl)
                .build();
    }

}
