package no.novari.flyt.archive.gateway.sak

import no.novari.fint.model.resource.arkiv.noark.SakResource
import no.novari.flyt.archive.gateway.resource.sak.CaseService
import no.novari.flyt.archive.gateway.resource.sak.InternalClientCaseController
import no.novari.flyt.archive.gateway.resource.sak.InternalClientCaseExceptionHandler
import no.novari.flyt.archive.gateway.resource.sak.InvalidArchiveInstanceIdException
import no.novari.flyt.webresourceserver.security.AuthorityMappingService
import no.novari.flyt.webresourceserver.security.SecurityFilterChainFactoryService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.security.oauth2.jwt.BadJwtException
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.web.client.RestClient
import java.time.Instant

/**
 * Runs the real security filter chain from flyt-web-resource-server against `/api/intern-klient`, over
 * real HTTP. MockMvc does not forward errors to `/error`, where that filter chain answers 401, so it
 * cannot catch an error status that turns into 401.
 * Only the token decoding is mocked; the subject of the decoded token is the OAuth client id.
 */
@SpringBootTest(
    classes = [InternalClientCaseControllerSecurityTest.TestApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@TestPropertySource(
    properties = [
        "novari.flyt.web-resource-server.security.api.internal-client.enabled=true",
        "novari.flyt.web-resource-server.security.api.internal-client.authorized-client-ids=$INTERNAL_CLIENT_ID",
    ],
)
class InternalClientCaseControllerSecurityTest {
    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import(
        InternalClientCaseController::class,
        InternalClientCaseExceptionHandler::class,
        AuthorityMappingService::class,
        SecurityFilterChainFactoryService::class,
    )
    class TestApplication

    @LocalServerPort
    var port: Int = 0

    @MockitoBean
    lateinit var caseService: CaseService

    @MockitoBean
    lateinit var jwtDecoder: JwtDecoder

    @BeforeEach
    fun setUp() {
        whenever(caseService.findByMappeId(any())).thenReturn(SakResource())
        whenever(caseService.findByArchiveInstanceIdWithFilteredJournalposts(any())).thenReturn(SakResource())
    }

    @ParameterizedTest
    @MethodSource("endpoints")
    fun `rejects request without token`(path: String) {
        assertThat(get(path, token = null)).isEqualTo(HttpStatus.UNAUTHORIZED)
    }

    @ParameterizedTest
    @MethodSource("endpoints")
    fun `rejects invalid token`(path: String) {
        whenever(jwtDecoder.decode(any())).thenThrow(BadJwtException("invalid"))

        assertThat(get(path)).isEqualTo(HttpStatus.UNAUTHORIZED)
    }

    @ParameterizedTest
    @MethodSource("endpoints")
    fun `rejects token from another client`(path: String) {
        givenTokenForClient("fint-flyt-some-other-oauth2-client")

        assertThat(get(path)).isEqualTo(HttpStatus.FORBIDDEN)
    }

    @ParameterizedTest
    @MethodSource("endpoints")
    fun `accepts token from internal client`(path: String) {
        givenTokenForClient(INTERNAL_CLIENT_ID)

        assertThat(get(path)).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `returns 404 when sak does not exist`() {
        givenTokenForClient(INTERNAL_CLIENT_ID)
        whenever(caseService.findByMappeId("2023/102")).thenReturn(null)

        assertThat(get(SAK_PATH)).isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    fun `returns 400 for invalid archiveInstanceId`() {
        givenTokenForClient(INTERNAL_CLIENT_ID)
        whenever(caseService.findByArchiveInstanceIdWithFilteredJournalposts(any()))
            .thenThrow(InvalidArchiveInstanceIdException("invalid"))

        assertThat(get(FILTERED_SAK_PATH)).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `returns 500 when FINT fails`() {
        givenTokenForClient(INTERNAL_CLIENT_ID)
        whenever(caseService.findByMappeId(any())).thenThrow(IllegalStateException("FINT failed"))

        assertThat(get(SAK_PATH)).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    private fun get(
        path: String,
        token: String? = "token",
    ): HttpStatusCode =
        RestClient
            .create("http://localhost:$port")
            .get()
            .uri(path)
            .headers { headers -> token?.let { headers.setBearerAuth(it) } }
            .exchange { _, response -> response.statusCode }

    private fun givenTokenForClient(clientId: String) {
        whenever(jwtDecoder.decode(any())).thenReturn(
            Jwt
                .withTokenValue("token")
                .header("alg", "none")
                .subject(clientId)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .build(),
        )
    }

    companion object {
        @JvmStatic
        fun endpoints(): List<String> = listOf(SAK_PATH, FILTERED_SAK_PATH)
    }
}

private const val INTERNAL_CLIENT_ID = "test-internal-client"
private const val SAK_PATH = "/api/intern-klient/arkiv/saker/2023/102"
private const val FILTERED_SAK_PATH = "/api/intern-klient/arkiv/saker?archiveInstanceId=2023/102-[1]"
