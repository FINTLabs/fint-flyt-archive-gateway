package no.novari.flyt.archive.gateway.dispatch.web.flytfile

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ClientHttpConnector
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient

@Configuration
@ConfigurationProperties(prefix = "novari.flyt.archive.gateway.client.fint-flyt-file")
class FlytFileWebClientConfiguration {
    var baseUrl: String? = null

    @Bean
    fun fileAuthorizedClientManager(
        clientRegistrationRepository: ClientRegistrationRepository,
        authorizedClientService: OAuth2AuthorizedClientService,
    ): OAuth2AuthorizedClientManager {
        val authorizedClientManager =
            AuthorizedClientServiceOAuth2AuthorizedClientManager(
                clientRegistrationRepository,
                authorizedClientService,
            )
        authorizedClientManager.setAuthorizedClientProvider(
            OAuth2AuthorizedClientProviderBuilder
                .builder()
                .clientCredentials()
                .refreshToken()
                .build(),
        )
        return authorizedClientManager
    }

    @Bean
    fun fileWebClient(
        fileAuthorizedClientManager: OAuth2AuthorizedClientManager,
        clientHttpConnector: ClientHttpConnector,
        webClientBuilder: WebClient.Builder,
    ): WebClient {
        val exchangeStrategies =
            ExchangeStrategies
                .builder()
                .codecs { it.defaultCodecs().maxInMemorySize(-1) }
                .build()

        val filter = ServletOAuth2AuthorizedClientExchangeFilterFunction(fileAuthorizedClientManager)
        filter.setDefaultClientRegistrationId("file-service")
        webClientBuilder.filter(filter)

        return webClientBuilder
            .clientConnector(clientHttpConnector)
            .baseUrl("${requireNotNull(baseUrl)}/api/intern-klient/filer")
            .exchangeStrategies(exchangeStrategies)
            .build()
    }
}
