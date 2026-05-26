package no.novari.flyt.archive.gateway

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.ClientHttpRequestFactory
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizationContext
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor
import org.springframework.web.client.RestClient

@Configuration
@ConfigurationProperties(prefix = "novari.flyt.archive.gateway.client.fint-archive")
class FintArchiveRestClientConfiguration {
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
    fun fintRestClient(
        restClientBuilder: RestClient.Builder,
        clientHttpRequestFactory: ClientHttpRequestFactory,
        @Qualifier("fintArchiveAuthorizedClientManager")
        authorizedClientManager: OAuth2AuthorizedClientManager?,
    ): RestClient {
        authorizedClientManager?.let { manager ->
            val interceptor = OAuth2ClientHttpRequestInterceptor(manager)
            interceptor.setClientRegistrationIdResolver { requireNotNull(registrationId) }
            restClientBuilder.requestInterceptor(interceptor)
        }

        return restClientBuilder
            .requestFactory(clientHttpRequestFactory)
            .baseUrl(requireNotNull(baseUrl))
            .build()
    }
}
