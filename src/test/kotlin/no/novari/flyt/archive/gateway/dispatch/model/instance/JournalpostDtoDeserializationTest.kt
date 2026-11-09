package no.novari.flyt.archive.gateway.dispatch.model.instance

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.novari.flyt.archive.gateway.dispatch.model.CaseDispatchType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class JournalpostDtoDeserializationTest {
    private val objectMapper = jacksonObjectMapper()

    @Test
    fun `deserializes avskrivning object from mapped instance`() {
        val archiveInstance =
            objectMapper.readValue<ArchiveInstance>(
                """
                {
                  "type": "BY_ID",
                  "caseId": "caseId",
                  "journalpost": [
                    {
                      "journalposttype": "https://example.com/journalposttype/I",
                      "avskrivning": {
                        "avskrivningsdato": "2026-08-24T09:12:48Z",
                        "avskrivningsmate": "TE"
                      }
                    }
                  ]
                }
                """.trimIndent(),
            )

        val avskrivning = archiveInstance.journalpost?.single()?.avskrivning
        assertThat(avskrivning?.avskrivningsmate).isEqualTo("TE")
        assertThat(avskrivning?.avskrivningsdato).isEqualTo("2026-08-24T09:12:48Z")
        assertThat(avskrivning?.getAvskrivningsmate()).contains("TE")
        assertThat(avskrivning?.getAvskrivningsdato()).contains("2026-08-24T09:12:48Z")
    }

    @Test
    fun `avskrivning remains optional for instances`() {
        val journalpostDto = objectMapper.readValue<JournalpostDto>("""{"tittel": "Journalpost"}""")

        assertThat(journalpostDto.avskrivning).isNull()
        assertThat(journalpostDto.getAvskrivning()).isEmpty()
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
