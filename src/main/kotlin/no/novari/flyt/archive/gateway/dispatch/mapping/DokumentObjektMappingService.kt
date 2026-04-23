package no.novari.flyt.archive.gateway.dispatch.mapping

import no.novari.fint.model.resource.Link
import no.novari.fint.model.resource.arkiv.noark.DokumentobjektResource
import no.novari.flyt.archive.gateway.dispatch.model.instance.DokumentobjektDto
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class DokumentObjektMappingService {
    fun toDokumentobjektResource(
        dokumentobjektDto: Collection<DokumentobjektDto>,
        fileArchiveLinkPerFileId: Map<UUID, Link>,
    ): List<DokumentobjektResource> = dokumentobjektDto.map { toDokumentobjektResource(it, fileArchiveLinkPerFileId) }

    fun toDokumentobjektResource(
        dokumentobjektDto: DokumentobjektDto,
        fileArchiveLinkPerFileId: Map<UUID, Link>,
    ): DokumentobjektResource =
        DokumentobjektResource().apply {
            dokumentobjektDto.variantformat?.let(Link::with)?.let(::addVariantFormat)
            dokumentobjektDto.filformat?.let(Link::with)?.let(::addFilformat)
            dokumentobjektDto.fileId?.let(fileArchiveLinkPerFileId::get)?.let(::addReferanseDokumentfil)
        }
}
