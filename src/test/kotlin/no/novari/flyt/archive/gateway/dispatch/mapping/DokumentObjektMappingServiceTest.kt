package no.novari.flyt.archive.gateway.dispatch.mapping

import no.novari.fint.model.resource.Link
import no.novari.flyt.archive.gateway.dispatch.model.instance.DokumentobjektDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class DokumentObjektMappingServiceTest {
    private lateinit var mappingService: DokumentObjektMappingService

    @BeforeEach
    fun setUp() {
        mappingService = DokumentObjektMappingService()
    }

    @Test
    fun `maps to DokumentobjektResource`() {
        val fileId = UUID.randomUUID()
        val dto =
            DokumentobjektDto(
                variantformat = "testVariantFormat",
                filformat = "testFilFormat",
                format = "testFormat",
                fileId = fileId,
            )
        val fileLink = Link("mockedLinkValue")

        val resource = mappingService.toDokumentobjektResource(dto, mapOf(fileId to fileLink))

        assertThat(resource.variantFormat).containsOnly(Link.with("testVariantFormat"))
        assertThat(resource.filformat).containsOnly(Link.with("testFilFormat"))
        assertThat(resource.referanseDokumentfil).contains(fileLink)
    }

    @Test
    fun `maps a list of dtos to a list of DokumentobjektResources`() {
        val fileId1 = UUID.randomUUID()
        val fileId2 = UUID.randomUUID()
        val dto1 =
            DokumentobjektDto(
                fileId = fileId1,
            )
        val dto2 =
            DokumentobjektDto(
                fileId = fileId2,
            )

        val resources =
            mappingService.toDokumentobjektResource(
                listOf(dto1, dto2),
                mapOf(fileId1 to Link("link1"), fileId2 to Link("link2")),
            )

        assertThat(resources).hasSize(2)
    }
}
