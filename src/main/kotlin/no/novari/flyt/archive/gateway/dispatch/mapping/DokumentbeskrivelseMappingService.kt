package no.novari.flyt.archive.gateway.dispatch.mapping

import no.novari.fint.model.resource.Link
import no.novari.fint.model.resource.arkiv.noark.DokumentbeskrivelseResource
import no.novari.flyt.archive.gateway.dispatch.model.instance.DokumentbeskrivelseDto
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class DokumentbeskrivelseMappingService(
    private val dokumentObjektMappingService: DokumentObjektMappingService,
    private val skjermingMappingService: SkjermingMappingService,
) {
    fun toDokumentbeskrivelseResource(
        dokumentbeskrivelseDto: Collection<DokumentbeskrivelseDto>,
        fileArchiveLinkPerFileId: Map<UUID, Link>,
    ): List<DokumentbeskrivelseResource> =
        dokumentbeskrivelseDto.map { toDokumentbeskrivelseResource(it, fileArchiveLinkPerFileId) }

    fun toDokumentbeskrivelseResource(
        dokumentbeskrivelseDto: DokumentbeskrivelseDto,
        fileArchiveLinkPerFileId: Map<UUID, Link>,
    ): DokumentbeskrivelseResource =
        DokumentbeskrivelseResource().apply {
            dokumentbeskrivelseDto.tittel?.let(::setTittel)
            dokumentbeskrivelseDto.dokumentType?.let(Link::with)?.let(::addDokumentType)
            dokumentbeskrivelseDto.tilknyttetRegistreringSom?.let(Link::with)?.let(::addTilknyttetRegistreringSom)
            dokumentbeskrivelseDto.dokumentstatus?.let(Link::with)?.let(::addDokumentstatus)
            dokumentbeskrivelseDto.dokumentobjekt
                ?.let { dokumentObjektMappingService.toDokumentobjektResource(it, fileArchiveLinkPerFileId) }
                ?.let(::setDokumentobjekt)
            dokumentbeskrivelseDto.skjerming
                ?.let(skjermingMappingService::toSkjermingResource)
                ?.let(::setSkjerming)
        }
}
