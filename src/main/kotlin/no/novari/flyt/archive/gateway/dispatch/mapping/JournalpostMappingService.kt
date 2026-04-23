package no.novari.flyt.archive.gateway.dispatch.mapping

import no.novari.fint.model.resource.Link
import no.novari.fint.model.resource.arkiv.noark.JournalpostResource
import no.novari.flyt.archive.gateway.dispatch.model.instance.JournalpostDto
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class JournalpostMappingService(
    private val skjermingMappingService: SkjermingMappingService,
    private val korrespondansepartMappingService: KorrespondansepartMappingService,
    private val dokumentbeskrivelseMappingService: DokumentbeskrivelseMappingService,
) {
    fun toJournalpostResource(
        journalpostDto: JournalpostDto,
        fileArchiveLinkPerFileId: Map<UUID, Link>,
    ): JournalpostResource =
        JournalpostResource().apply {
            journalpostDto.tittel?.let(::setTittel)
            journalpostDto.offentligTittel?.let(::setOffentligTittel)
            journalpostDto.journalstatus?.let(Link::with)?.let(::addJournalstatus)
            journalpostDto.tilgangsgruppe?.let(Link::with)?.let(::addTilgangsgruppe)
            journalpostDto.saksbehandler?.let(Link::with)?.let(::addSaksbehandler)
            journalpostDto.journalposttype?.let(Link::with)?.let(::addJournalposttype)
            journalpostDto.administrativEnhet?.let(Link::with)?.let(::addAdministrativEnhet)
            journalpostDto.skjerming?.let(skjermingMappingService::toSkjermingResource)?.let(::setSkjerming)
            journalpostDto.korrespondansepart
                ?.let(korrespondansepartMappingService::toKorrespondansepartResource)
                ?.let(::setKorrespondansepart)
            journalpostDto.dokumentbeskrivelse
                ?.let { dokumentbeskrivelseMappingService.toDokumentbeskrivelseResource(it, fileArchiveLinkPerFileId) }
                ?.let(::setDokumentbeskrivelse)
        }
}
