package no.novari.flyt.archive.gateway.dispatch.mapping

import no.novari.cache.FintCache
import no.novari.fint.model.arkiv.noark.Avskrivning
import no.novari.fint.model.resource.arkiv.kodeverk.JournalpostTypeResource
import no.novari.flyt.archive.gateway.dispatch.model.instance.JournalpostDto
import no.novari.flyt.archive.gateway.links.ResourceLinkUtil
import no.novari.flyt.archive.gateway.resource.web.FintArchiveResourceClient
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import java.util.Date

@Service
class AvskrivningMappingService(
    private val journalpostTypeResourceCache: FintCache<String, JournalpostTypeResource>,
    private val fintArchiveResourceClient: FintArchiveResourceClient,
) {
    fun toAvskrivning(journalpostDto: JournalpostDto): Avskrivning? {
        val dto = journalpostDto.avskrivning ?: return null
        if (dto.avskrivningsmate.isNullOrBlank()) return null
        val journalposttype = journalpostDto.journalposttype ?: return null
        if (getJournalpostTypeCode(journalposttype) != INCOMING_JOURNALPOST_TYPE) return null

        return Avskrivning().apply {
            avskrivningsmate = dto.avskrivningsmate
            avskrivningsdato = toDate(dto.avskrivningsdato)
        }
    }

    private fun getJournalpostTypeCode(journalposttype: String): String {
        journalpostTypeResourceCache.get(journalposttype)?.kode?.let { return it }

        val types =
            try {
                fintArchiveResourceClient.getResourcesSince(
                    "arkiv/kodeverk/journalposttype",
                    JournalpostTypeResource::class.java,
                    0L,
                )
            } catch (error: RuntimeException) {
                throw JournalpostTypeLookupException(journalposttype, error)
            }
        if (types.isEmpty()) throw JournalpostTypeLookupException(journalposttype)

        types.forEach { journalpostTypeResourceCache.put(ResourceLinkUtil.getSelfLinks(it), it) }
        val type =
            types.firstOrNull { journalposttype in ResourceLinkUtil.getSelfLinks(it) }
                ?: throw InvalidJournalpostTypeException(journalposttype)
        return type.kode ?: throw JournalpostTypeLookupException(journalposttype)
    }

    private fun toDate(value: String?): Date {
        if (value.isNullOrBlank()) {
            return Date.from(Instant.now().truncatedTo(ChronoUnit.SECONDS))
        }
        if (!DATE_TIME_PATTERN.matches(value)) throw InvalidAvskrivningsdatoException(value)
        val instant =
            runCatching { OffsetDateTime.parse(value).toInstant() }
                .getOrElse { throw InvalidAvskrivningsdatoException(value) }
        return Date.from(instant)
    }

    companion object {
        private const val INCOMING_JOURNALPOST_TYPE = "I"
        private val DATE_TIME_PATTERN = Regex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z")
    }
}
