package no.novari.flyt.archive.gateway.dispatch.web.flytfile

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.ClientHttpRequestFactory
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor
import org.springframework.web.client.RestClient

@Configuration
@ConfigurationProperties(prefix = "novari.flyt.archive.gateway.client.fint-flyt-file")
class FlytFileRestClientConfiguration {
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
    fun fileRestClient(
        fileAuthorizedClientManager: OAuth2AuthorizedClientManager,
        clientHttpRequestFactory: ClientHttpRequestFactory,
        restClientBuilder: RestClient.Builder,
    ): RestClient {
        val interceptor = OAuth2ClientHttpRequestInterceptor(fileAuthorizedClientManager)
        interceptor.setClientRegistrationIdResolver { "file-service" }

        return restClientBuilder
            .requestInterceptor(interceptor)
            .requestFactory(clientHttpRequestFactory)
            .baseUrl("${requireNotNull(baseUrl)}/api/intern-klient/filer")
            .build()
    }
}
