package no.novari.flyt.archive.gateway.resource.kodeverk

import no.novari.cache.FintCache
import no.novari.fint.model.felles.kompleksedatatyper.Identifikator
import no.novari.fint.model.resource.Link
import no.novari.fint.model.resource.arkiv.noark.ArkivressursResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class CodelistControllerTest {
    private lateinit var arkivressursResourceCache: FintCache<String, ArkivressursResource>
    private lateinit var arkivressursDisplayNameMapper: ArkivressursDisplayNameMapper
    private lateinit var codelistController: CodelistController

    @BeforeEach
    fun setUp() {
        arkivressursResourceCache = mock()
        arkivressursDisplayNameMapper = mock()
        codelistController =
            CodelistController(
                mock(),
                mock(),
                arkivressursResourceCache,
                mock(),
                mock(),
                mock(),
                mock(),
                mock(),
                mock(),
                mock(),
                mock(),
                mock(),
                mock(),
                mock(),
                mock(),
                mock(),
                mock(),
                mock(),
                arkivressursDisplayNameMapper,
            )
    }

    @Test
    fun `getArkivressurs returns split display name fields`() {
        val arkivressurs =
            arkivressurs(
                selfLink = "https://beta.felleskomponent.no/arkiv/noark/arkivressurs/systemid/18693",
                systemId = "18693",
            )
        whenever(arkivressursResourceCache.getAllDistinct()).thenReturn(listOf(arkivressurs))
        whenever(arkivressursDisplayNameMapper.findPersonalressursBrukernavn(arkivressurs))
            .thenReturn(Optional.of("EGIBAL"))
        whenever(arkivressursDisplayNameMapper.findPersonNavn(arkivressurs))
            .thenReturn(Optional.of("Egil Ballestad"))

        val response = codelistController.getArkivressurs()

        assertThat(response.body)
            .containsExactly(
                ResourceReference(
                    id = "https://beta.felleskomponent.no/arkiv/noark/arkivressurs/systemid/18693",
                    displayName = "[EGIBAL] Egil Ballestad #18693",
                    functionalId = "EGIBAL",
                    name = "Egil Ballestad",
                    technicalId = "18693",
                ),
            )
    }

    @Test
    fun `getArkivressurs returns technical id when no resource match is found`() {
        val arkivressurs =
            arkivressurs(
                selfLink = "https://beta.felleskomponent.no/arkiv/noark/arkivressurs/systemid/18699",
                systemId = "18699",
            )
        whenever(arkivressursResourceCache.getAllDistinct()).thenReturn(listOf(arkivressurs))
        whenever(arkivressursDisplayNameMapper.findPersonalressursBrukernavn(arkivressurs))
            .thenReturn(Optional.empty())
        whenever(arkivressursDisplayNameMapper.findPersonNavn(arkivressurs))
            .thenReturn(Optional.empty())

        val response = codelistController.getArkivressurs()

        assertThat(response.body)
            .containsExactly(
                ResourceReference(
                    id = "https://beta.felleskomponent.no/arkiv/noark/arkivressurs/systemid/18699",
                    displayName = "#18699",
                    technicalId = "18699",
                ),
            )
    }

    private fun arkivressurs(
        selfLink: String,
        systemId: String,
    ): ArkivressursResource {
        val arkivressurs: ArkivressursResource = mock()
        whenever(arkivressurs.selfLinks).thenReturn(listOf(Link.with(selfLink)))
        whenever(arkivressurs.systemId).thenReturn(
            Identifikator().apply {
                identifikatorverdi = systemId
            },
        )

        return arkivressurs
    }
}
