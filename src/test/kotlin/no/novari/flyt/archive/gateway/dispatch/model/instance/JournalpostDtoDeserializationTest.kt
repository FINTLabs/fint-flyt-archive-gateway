package no.novari.flyt.archive.gateway.dispatch.model.instance

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.novari.flyt.archive.gateway.dispatch.model.CaseDispatchType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class JournalpostDtoDeserializationTest {
    private val objectMapper = jacksonObjectMapper()

    @Test
    fun `deserializes avskrivningsmate from mapped instance`() {
        val archiveInstance =
            objectMapper.readValue<ArchiveInstance>(
                """
                {
                  "type": "BY_ID",
                  "caseId": "caseId",
                  "journalpost": [
                    {
                      "journalposttype": "https://example.com/journalposttype/I",
                      "avskrivningsmate": "TE"
                    }
                  ]
                }
                """.trimIndent(),
            )

        assertThat(archiveInstance.journalpost?.single()?.avskrivningsmate).isEqualTo("TE")
        assertThat(archiveInstance.journalpost?.single()?.getAvskrivningsmate()).contains("TE")
    }

    @Test
    fun `avskrivningsmate remains optional for existing instances`() {
        val journalpostDto = objectMapper.readValue<JournalpostDto>("""{"tittel": "Journalpost"}""")

        assertThat(journalpostDto.avskrivningsmate).isNull()
        assertThat(journalpostDto.getAvskrivningsmate()).isEmpty()
    }

    @Test
    fun `deserializes dokumentetsDato from mapped instance date`() {
        val archiveInstance =
            objectMapper.readValue<ArchiveInstance>(
                """
                {
                  "type": "BY_ID",
                  "caseId": "caseId",
                  "journalpost": [
                    {
                      "tittel": "Journalpost",
                      "dokumentetsDato": "2026-08-24"
                    }
                  ]
                }
                """.trimIndent(),
            )

        assertThat(archiveInstance.type).isEqualTo(CaseDispatchType.BY_ID)
        assertThat(archiveInstance.journalpost?.first()?.dokumentetsDato).isEqualTo("2026-08-24")
    }

    @Test
    fun `deserializes dokumentetsDato without parsing it`() {
        val journalpostDto =
            objectMapper.readValue<JournalpostDto>(
                """
                {
                  "dokumentetsDato": "not a date"
                }
                """.trimIndent(),
            )

        assertThat(journalpostDto.dokumentetsDato).isEqualTo("not a date")
    }
}
