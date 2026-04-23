package no.novari.flyt.archive.gateway

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ClientHttpConnector
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizationContext
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient

@Configuration
@ConfigurationProperties(prefix = "novari.flyt.archive.gateway.client.fint-archive")
class FintArchiveWebClientConfiguration {
    var baseUrl: String? = null
    var username: String? = null
    var password: String? = null
    var registrationId: String? = null

    @Bean
    @ConditionalOnProperty(
        name = ["novari.flyt.archive.gateway.client.fint-archive.authorization.enable"],
        havingValue = "true",
    )
    fun fintArchiveAuthorizedClientManager(
        clientRegistrationRepository: ClientRegistrationRepository,
        authorizedClientService: OAuth2AuthorizedClientService,
    ): OAuth2AuthorizedClientManager {
        val authorizedClientManager =
            AuthorizedClientServiceOAuth2AuthorizedClientManager(
                clientRegistrationRepository,
                authorizedClientService,
            )
        // TODO: migrate off the deprecated password grant when FINT IdP supports a supported flow.
        authorizedClientManager.setAuthorizedClientProvider(
            OAuth2AuthorizedClientProviderBuilder
                .builder()
                .password()
                .refreshToken()
                .build(),
        )
        authorizedClientManager.setContextAttributesMapper { _: OAuth2AuthorizeRequest ->
            mapOf(
                OAuth2AuthorizationContext.USERNAME_ATTRIBUTE_NAME to requireNotNull(username),
                OAuth2AuthorizationContext.PASSWORD_ATTRIBUTE_NAME to requireNotNull(password),
            )
        }
        return authorizedClientManager
    }

    @Bean
    fun fintWebClient(
        webClientBuilder: WebClient.Builder,
        @Qualifier("fintArchiveAuthorizedClientManager")
        authorizedClientManager: OAuth2AuthorizedClientManager?,
        clientHttpConnector: ClientHttpConnector,
    ): WebClient {
        val exchangeStrategies =
            ExchangeStrategies
                .builder()
                .codecs { it.defaultCodecs().maxInMemorySize(-1) }
                .build()

        authorizedClientManager?.let { manager ->
            val filter = ServletOAuth2AuthorizedClientExchangeFilterFunction(manager)
            filter.setDefaultClientRegistrationId(requireNotNull(registrationId))
            webClientBuilder.filter(filter)
        }

        return webClientBuilder
            .clientConnector(clientHttpConnector)
            .exchangeStrategies(exchangeStrategies)
            .baseUrl(requireNotNull(baseUrl))
            .build()
    }
}
