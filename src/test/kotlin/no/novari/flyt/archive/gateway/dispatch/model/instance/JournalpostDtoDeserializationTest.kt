package no.novari.flyt.archive.gateway.dispatch.model.instance

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.novari.flyt.archive.gateway.dispatch.model.CaseDispatchType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class JournalpostDtoDeserializationTest {
    private val objectMapper = jacksonObjectMapper()

    @Test
    fun `deserializes dokumentetsDato from mapped instance date time`() {
        val archiveInstance =
            objectMapper.readValue<ArchiveInstance>(
                """
                {
                  "type": "BY_ID",
                  "caseId": "caseId",
                  "journalpost": [
                    {
                      "tittel": "Journalpost",
                      "dokumentetsDato": "08/24/2026 09:12:48"
                    }
                  ]
                }
                """.trimIndent(),
            )

        assertThat(archiveInstance.type).isEqualTo(CaseDispatchType.BY_ID)
        assertThat(archiveInstance.journalpost?.first()?.dokumentetsDato).isEqualTo("08/24/2026 09:12:48")
    }

    @Test
    fun `deserializes invalid dokumentetsDato without failing kafka deserialization`() {
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
