package no.novari.flyt.archive.gateway.dispatch.mapping

import com.fasterxml.jackson.databind.ObjectMapper
import no.novari.cache.FintCache
import no.novari.fint.model.resource.Link
import no.novari.fint.model.resource.arkiv.kodeverk.JournalpostTypeResource
import no.novari.fint.model.resource.arkiv.noark.SakResource
import no.novari.flyt.archive.gateway.FintArchiveJacksonConfiguration
import no.novari.flyt.archive.gateway.dispatch.model.instance.AvskrivningDto
import no.novari.flyt.archive.gateway.dispatch.model.instance.DokumentbeskrivelseDto
import no.novari.flyt.archive.gateway.dispatch.model.instance.JournalpostDto
import no.novari.flyt.archive.gateway.dispatch.model.instance.KorrespondansepartDto
import no.novari.flyt.archive.gateway.dispatch.model.instance.SkjermingDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import java.time.Instant

class JournalpostMappingServiceTest {
    private val objectMapper =
        Jackson2ObjectMapperBuilder
            .json()
            .also { FintArchiveJacksonConfiguration().avskrivningSerialization().customize(it) }
            .build<ObjectMapper>()

    private val journalpostTypeResourceCache: FintCache<String, JournalpostTypeResource> =
        mock<FintCache<String, JournalpostTypeResource>>().also {
            whenever(it.get("incoming-type")).thenReturn(JournalpostTypeResource().apply { kode = "I" })
        }

    private val journalpostMappingService =
        JournalpostMappingService(
            SkjermingMappingService(),
            KorrespondansepartMappingService(
                AdresseMappingService(),
                KontaktinformasjonMappingService(),
                SkjermingMappingService(),
            ),
            DokumentbeskrivelseMappingService(DokumentObjektMappingService(), SkjermingMappingService()),
            DokumentetsDatoMappingService(),
            AvskrivningMappingService(journalpostTypeResourceCache, mock()),
        )

    @Test
    fun `maps to JournalpostResource`() {
        val skjermingDto =
            SkjermingDto
                .builder()
                .tilgangsrestriksjon("Tilgangsrestriksjon")
                .skjermingshjemmel("Skjermingshjemmel")
                .build()

        val dto =
            JournalpostDto
                .builder()
                .tittel("Tittel")
                .offentligTittel("Offentlig tittel")
                .journalposttype("incoming-type")
                .avskrivning(AvskrivningDto(avskrivningsdato = "2026-08-24T09:12:48Z", avskrivningsmate = "TE"))
                .administrativEnhet("Administrativ enhet")
                .saksbehandler("Saksbehandler")
                .journalstatus("Journalstatus")
                .dokumentetsDato("2026-08-28")
                .tilgangsgruppe("Tilgangsgruppe")
                .skjerming(skjermingDto)
                .korrespondansepart(listOf(KorrespondansepartDto.builder().korrespondanseparttype("type").build()))
                .dokumentbeskrivelse(listOf(DokumentbeskrivelseDto.builder().tittel("dok").build()))
                .build()

        val mappedResource = journalpostMappingService.toJournalpostResource(dto, emptyMap())

        assertThat(mappedResource.tittel).isEqualTo("Tittel")
        assertThat(mappedResource.offentligTittel).isEqualTo("Offentlig tittel")
        assertThat(mappedResource.journalposttype.first().href).isEqualTo("incoming-type")
        assertThat(mappedResource.avskrivning.avskrivningsmate).isEqualTo("TE")
        assertThat(mappedResource.avskrivning.avskrivningsdato.toInstant())
            .isEqualTo(Instant.parse("2026-08-24T09:12:48Z"))
        assertThat(mappedResource.administrativEnhet.first().href).isEqualTo("Administrativ enhet")
        assertThat(mappedResource.saksbehandler.first().href).isEqualTo("Saksbehandler")
        assertThat(mappedResource.journalstatus.first().href).isEqualTo("Journalstatus")
        assertThat(mappedResource.dokumentetsDato.toInstant()).isEqualTo(Instant.parse("2026-08-28T12:00:00Z"))
        assertThat(mappedResource.tilgangsgruppe.first().href).isEqualTo("Tilgangsgruppe")
        assertThat(mappedResource.skjerming.tilgangsrestriksjon).contains(Link.with("Tilgangsrestriksjon"))
        assertThat(mappedResource.skjerming.skjermingshjemmel).contains(Link.with("Skjermingshjemmel"))
    }

    @ParameterizedTest
    @ValueSource(strings = ["BU", "BE", "TLF", "TE", "TO", "custom value"])
    fun `maps avskrivningsmate as a value in avskrivning`(avskrivningsmate: String) {
        val dto =
            JournalpostDto(
                journalposttype = "incoming-type",
                avskrivning = AvskrivningDto(avskrivningsmate = avskrivningsmate),
            )

        val mappedResource = journalpostMappingService.toJournalpostResource(dto, emptyMap())

        assertThat(mappedResource.avskrivning.avskrivningsmate).isEqualTo(avskrivningsmate)
    }

    @Test
    fun `leaves avskrivning unset when avskrivningsmate is absent`() {
        val mappedResource = journalpostMappingService.toJournalpostResource(JournalpostDto(), emptyMap())

        assertThat(mappedResource.avskrivning).isNull()
    }

    @Test
    fun `omits entire avskrivning from serialized journalpost for other types`() {
        whenever(journalpostTypeResourceCache.get("outgoing-type"))
            .thenReturn(JournalpostTypeResource().apply { kode = "U" })
        val dto =
            JournalpostDto(
                journalposttype = "outgoing-type",
                avskrivning = AvskrivningDto("invalid date", "TE"),
            )

        val resource = journalpostMappingService.toJournalpostResource(dto, emptyMap())
        val json = objectMapper.readTree(objectMapper.writeValueAsString(resource))

        assertThat(json.has("avskrivning")).isFalse()
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "2026-08-24T09:12:48Z",
            "2026-08-24T09:12:48.000Z",
            "2026-08-24T09:12:48.987Z",
            "2026-08-24T11:12:48+02:00",
            "2026-08-24T11:12:48.987+02:00",
            "2026-08-24T04:12:48.987-05:00",
        ],
    )
    fun `serializes avskrivning date in UTC with second precision without avskrevetAv`(date: String) {
        val dto =
            JournalpostDto(
                journalposttype = "incoming-type",
                avskrivning = AvskrivningDto(date, "TE"),
            )

        val resource = journalpostMappingService.toJournalpostResource(dto, emptyMap())
        val json = objectMapper.readTree(objectMapper.writeValueAsString(resource))["avskrivning"]

        assertThat(json["avskrivningsdato"].asText()).isEqualTo("2026-08-24T09:12:48Z")
        assertThat(json["avskrivningsmate"].asText()).isEqualTo("TE")
        assertThat(json.has("avskrevetAv")).isFalse()
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "2026-11-09T12:58:53.000+00:00",
            "2026-11-09T12:58:53Z",
            "2026-11-09T13:58:53.000+01:00",
        ],
    )
    fun `reads avskrivningsdato from archive case response with milliseconds and offsets`(date: String) {
        val json =
            """
            {
              "journalpost": [{
                "avskrivning": {
                  "avskrivningsdato": "$date",
                  "avskrivningsmate": "TE"
                }
              }]
            }
            """.trimIndent()

        val resource = objectMapper.readValue(json, SakResource::class.java)
        val avskrivning = resource.journalpost.single().avskrivning

        assertThat(avskrivning.avskrivningsdato.toInstant())
            .isEqualTo(Instant.parse("2026-11-09T12:58:53Z"))
    }
}
