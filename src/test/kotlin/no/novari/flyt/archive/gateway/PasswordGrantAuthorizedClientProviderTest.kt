package no.novari.flyt.archive.gateway

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.converter.FormHttpMessageConverter
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.oauth2.client.OAuth2AuthorizationContext
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.ClientAuthenticationMethod
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.security.oauth2.core.OAuth2RefreshToken
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter
import org.springframework.test.web.client.ExpectedCount.never
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.content
import org.springframework.test.web.client.match.MockRestRequestMatchers.header
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient
import java.time.Instant
import java.util.Base64

class PasswordGrantAuthorizedClientProviderTest {
    private val builder =
        RestClient
            .builder()
            .configureMessageConverters { converters ->
                converters.addCustomConverter(FormHttpMessageConverter())
                converters.addCustomConverter(OAuth2AccessTokenResponseHttpMessageConverter())
            }
    private val server = MockRestServiceServer.bindTo(builder).build()
    private val provider = PasswordGrantAuthorizedClientProvider("user", "secret password", builder.build())

    @Test
    fun `gets a token with the password grant and client_secret_basic`() {
        server
            .expect(requestTo(TOKEN_URI))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Basic " + base64("client:client-secret")))
            .andExpect(
                content().formDataContains(
                    mapOf("grant_type" to "password", "username" to "user", "password" to "secret password"),
                ),
            ).andRespond(withSuccess(TOKEN_RESPONSE, MediaType.APPLICATION_JSON))

        val client = provider.authorize(context(registration()))

        server.verify()
        assertThat(client!!.accessToken.tokenValue).isEqualTo("access-1")
        assertThat(client.refreshToken!!.tokenValue).isEqualTo("refresh-1")
        assertThat(client.principalName).isEqualTo("fint-archive-client")
    }

    @Test
    fun `sends the client credentials in the body with client_secret_post`() {
        server
            .expect(requestTo(TOKEN_URI))
            .andExpect(
                content().formDataContains(
                    mapOf("grant_type" to "password", "client_id" to "client", "client_secret" to "client-secret"),
                ),
            ).andRespond(withSuccess(TOKEN_RESPONSE, MediaType.APPLICATION_JSON))

        provider.authorize(context(registration(ClientAuthenticationMethod.CLIENT_SECRET_POST)))

        server.verify()
    }

    @Test
    fun `leaves a valid token, and an expired token with a refresh token, to the other providers`() {
        server.expect(never(), requestTo(TOKEN_URI))
        val registration = registration()

        assertThat(
            provider.authorize(context(registration, authorizedClient(registration, Instant.now().plusSeconds(300)))),
        ).isNull()
        assertThat(
            provider.authorize(
                context(
                    registration,
                    authorizedClient(registration, Instant.now().minusSeconds(1), OAuth2RefreshToken("r", null)),
                ),
            ),
        ).isNull()
        server.verify()
    }

    @Test
    fun `gets a new token when the token has expired and there is no refresh token`() {
        server.expect(requestTo(TOKEN_URI)).andRespond(withSuccess(TOKEN_RESPONSE, MediaType.APPLICATION_JSON))
        val registration = registration()

        val client =
            provider.authorize(
                context(registration, authorizedClient(registration, Instant.now().minusSeconds(1))),
            )

        server.verify()
        assertThat(client!!.accessToken.tokenValue).isEqualTo("access-1")
    }

    @Test
    fun `ignores registrations with another grant type`() {
        server.expect(never(), requestTo(TOKEN_URI))
        val clientCredentials =
            ClientRegistration
                .withRegistrationId("other")
                .clientId("client")
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .tokenUri(TOKEN_URI)
                .build()

        assertThat(provider.authorize(context(clientCredentials))).isNull()
        server.verify()
    }

    private fun registration(method: ClientAuthenticationMethod = ClientAuthenticationMethod.CLIENT_SECRET_BASIC) =
        ClientRegistration
            .withRegistrationId("fint")
            .clientId("client")
            .clientSecret("client-secret")
            .clientAuthenticationMethod(method)
            .authorizationGrantType(PasswordGrantAuthorizedClientProvider.PASSWORD)
            .tokenUri(TOKEN_URI)
            .build()

    private fun authorizedClient(
        registration: ClientRegistration,
        expiresAt: Instant,
        refreshToken: OAuth2RefreshToken? = null,
    ) = OAuth2AuthorizedClient(
        registration,
        "fint-archive-client",
        OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "old", expiresAt.minusSeconds(3600), expiresAt),
        refreshToken,
    )

    private fun context(
        registration: ClientRegistration,
        authorizedClient: OAuth2AuthorizedClient? = null,
    ): OAuth2AuthorizationContext {
        val principal = TestingAuthenticationToken("fint-archive-client", null)
        return if (authorizedClient != null) {
            OAuth2AuthorizationContext.withAuthorizedClient(authorizedClient).principal(principal).build()
        } else {
            OAuth2AuthorizationContext.withClientRegistration(registration).principal(principal).build()
        }
    }

    private fun base64(value: String) = Base64.getEncoder().encodeToString(value.toByteArray())

    private companion object {
        const val TOKEN_URI = "https://idp.test/nidp/oauth/nam/token"
        const val TOKEN_RESPONSE =
            """{"access_token":"access-1","token_type":"Bearer","expires_in":1800,"refresh_token":"refresh-1"}"""
    }
}
