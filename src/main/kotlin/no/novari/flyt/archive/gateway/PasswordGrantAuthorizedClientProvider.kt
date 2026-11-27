package no.novari.flyt.archive.gateway

import org.springframework.http.converter.FormHttpMessageConverter
import org.springframework.security.oauth2.client.OAuth2AuthorizationContext
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider
import org.springframework.security.oauth2.client.endpoint.AbstractOAuth2AuthorizationGrantRequest
import org.springframework.security.oauth2.client.endpoint.DefaultOAuth2TokenRequestHeadersConverter
import org.springframework.security.oauth2.client.endpoint.DefaultOAuth2TokenRequestParametersConverter
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.OAuth2Token
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import java.time.Clock
import java.time.Duration

/**
 * The OAuth 2.0 password grant (ROPC), which Spring Security 7 removed together with its
 * `PasswordOAuth2AuthorizedClientProvider`. It follows that provider: it gets a token when there is none, or when the
 * token has expired and there is no refresh token. Otherwise it returns `null`, so `refreshToken()` in the same
 * `OAuth2AuthorizedClientProviderBuilder` refreshes it. The token request is built with Spring's converters, so client
 * authentication follows the registration (`client_secret_basic` or `client_secret_post`).
 *
 * The same class is in fint-flyt-egrunnerverv-gateway, for ServiceNow.
 *
 * shortcut: own code for a grant OAuth 2.1 removes, delete it when the FINT IdP gives the archive client a token with
 * client credentials.
 */
class PasswordGrantAuthorizedClientProvider(
    private val username: String,
    private val password: String,
    private val restClient: RestClient = defaultTokenRestClient(),
    private val clock: Clock = Clock.systemUTC(),
    private val clockSkew: Duration = Duration.ofSeconds(60),
) : OAuth2AuthorizedClientProvider {
    override fun authorize(context: OAuth2AuthorizationContext): OAuth2AuthorizedClient? {
        val registration = context.clientRegistration
        if (registration.authorizationGrantType != PASSWORD) return null

        val authorizedClient = context.authorizedClient
        if (authorizedClient != null &&
            (!hasExpired(authorizedClient.accessToken) || authorizedClient.refreshToken != null)
        ) {
            return null
        }

        val request = PasswordGrantRequest(registration)
        val parameters =
            requireNotNull(
                DefaultOAuth2TokenRequestParametersConverter<PasswordGrantRequest>().convert(request),
            ).apply {
                set("username", username)
                set("password", password)
                if (registration.scopes.isNotEmpty()) {
                    set(
                        OAuth2ParameterNames.SCOPE,
                        registration.scopes.joinToString(" "),
                    )
                }
            }
        val response =
            restClient
                .post()
                .uri(registration.providerDetails.tokenUri)
                .headers {
                    it.addAll(
                        requireNotNull(
                            DefaultOAuth2TokenRequestHeadersConverter<PasswordGrantRequest>().convert(request),
                        ),
                    )
                }.body(parameters)
                .retrieve()
                .body<OAuth2AccessTokenResponse>()
                ?: error("Empty token response from ${registration.providerDetails.tokenUri}")

        return OAuth2AuthorizedClient(registration, context.principal.name, response.accessToken, response.refreshToken)
    }

    private fun hasExpired(token: OAuth2Token): Boolean {
        val expiresAt = token.expiresAt ?: return false
        return !clock.instant().isBefore(expiresAt.minus(clockSkew))
    }

    private class PasswordGrantRequest(
        registration: ClientRegistration,
    ) : AbstractOAuth2AuthorizationGrantRequest(PASSWORD, registration)

    companion object {
        val PASSWORD = AuthorizationGrantType("password")

        // The converters and error handler of Spring's own token response clients.
        fun defaultTokenRestClient(): RestClient =
            RestClient
                .builder()
                .configureMessageConverters { converters ->
                    converters.addCustomConverter(FormHttpMessageConverter())
                    converters.addCustomConverter(OAuth2AccessTokenResponseHttpMessageConverter())
                }.defaultStatusHandler(OAuth2ErrorResponseErrorHandler())
                .build()
    }
}
