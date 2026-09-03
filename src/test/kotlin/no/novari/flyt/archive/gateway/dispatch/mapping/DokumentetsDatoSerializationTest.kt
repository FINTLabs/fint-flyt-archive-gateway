package no.novari.flyt.archive.gateway.dispatch.mapping

import com.fasterxml.jackson.databind.ObjectMapper
import no.novari.fint.model.resource.arkiv.noark.JournalpostResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import java.time.Instant
import java.util.Date

@JsonTest
class DokumentetsDatoSerializationTest(
    @Autowired private val objectMapper: ObjectMapper,
) {
    @Test
    fun `serializes dokumentetsDato as ISO date time in UTC`() {
        val journalpostResource =
            JournalpostResource().apply {
                dokumentetsDato = Date.from(Instant.parse("1990-03-29T12:00:00Z"))
            }

        val json = objectMapper.writeValueAsString(journalpostResource)

        assertThat(json).contains("\"dokumentetsDato\":\"1990-03-29T12:00:00Z\"")
    }
}
