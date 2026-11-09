package no.novari.flyt.archive.gateway.sak

import no.novari.fint.model.resource.arkiv.noark.SakResource
import no.novari.flyt.archive.gateway.resource.sak.CaseService
import no.novari.flyt.archive.gateway.resource.sak.InternalClientCaseController
import no.novari.flyt.webresourceserver.security.AuthorityMappingService
import no.novari.flyt.webresourceserver.security.InternalClientApiConfiguration
import no.novari.flyt.webresourceserver.security.SecurityConfiguration
import no.novari.flyt.webresourceserver.security.SecurityFilterChainFactoryService
import no.novari.flyt.webresourceserver.security.client.sourceapplication.SourceApplicationJwtConverter
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.ComponentScan.Filter
import org.springframework.context.annotation.FilterType
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.security.oauth2.jwt.BadJwtException
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant

/**
 * Runs the real security filter chain from flyt-web-resource-server against `/api/intern-klient`.
 * Only the token decoding is mocked; the subject of the decoded token is the OAuth client id.
 *
 * The MVC slice picks up [SourceApplicationJwtConverter] as a Converter. It belongs to the external API and is
 * excluded because its dependencies are not needed here.
 */
@WebMvcTest(
    controllers = [InternalClientCaseController::class],
    excludeFilters = [Filter(type = FilterType.ASSIGNABLE_TYPE, classes = [SourceApplicationJwtConverter::class])],
)
@ImportAutoConfiguration(InternalClientApiConfiguration::class, SecurityConfiguration::class)
@Import(AuthorityMappingService::class, SecurityFilterChainFactoryService::class)
@TestPropertySource(
    properties = [
        "novari.flyt.web-resource-server.security.api.internal-client.enabled=true",
        "novari.flyt.web-resource-server.security.api.internal-client.authorized-client-ids=$ARCHIVE_CLIENT_ID",
    ],
)
class InternalClientCaseControllerSecurityTest {
    @Autowired
    lateinit var mockMvc: MockMvc

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
        mockMvc
            .perform(get(path))
            .andExpect(status().isUnauthorized)
    }

    @ParameterizedTest
    @MethodSource("endpoints")
    fun `rejects invalid token`(path: String) {
        whenever(jwtDecoder.decode(any())).thenThrow(BadJwtException("invalid"))

        mockMvc
            .perform(get(path).header(HttpHeaders.AUTHORIZATION, "Bearer token"))
            .andExpect(status().isUnauthorized)
    }

    @ParameterizedTest
    @MethodSource("endpoints")
    fun `rejects token from another client`(path: String) {
        givenTokenForClient("fint-flyt-some-other-oauth2-client")

        mockMvc
            .perform(get(path).header(HttpHeaders.AUTHORIZATION, "Bearer token"))
            .andExpect(status().isForbidden)
    }

    @ParameterizedTest
    @MethodSource("endpoints")
    fun `accepts token from archive client`(path: String) {
        givenTokenForClient(ARCHIVE_CLIENT_ID)

        mockMvc
            .perform(get(path).header(HttpHeaders.AUTHORIZATION, "Bearer token"))
            .andExpect(status().isOk)
    }

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
        fun endpoints(): List<String> =
            listOf(
                "/api/intern-klient/arkiv/saker/2023/102",
                "/api/intern-klient/arkiv/saker?archiveInstanceId=2023/102-[1]",
            )
    }
}

private const val ARCHIVE_CLIENT_ID = "test-archive-client"
