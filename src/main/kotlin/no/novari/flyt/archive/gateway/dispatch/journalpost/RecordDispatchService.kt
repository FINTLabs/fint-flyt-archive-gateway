package no.novari.flyt.archive.gateway.dispatch.journalpost

import io.netty.handler.timeout.ReadTimeoutException
import no.novari.fint.model.resource.Link
import no.novari.fint.model.resource.arkiv.noark.JournalpostResource
import no.novari.flyt.archive.gateway.dispatch.file.FilesDispatchService
import no.novari.flyt.archive.gateway.dispatch.journalpost.result.RecordDispatchResult
import no.novari.flyt.archive.gateway.dispatch.mapping.JournalpostMappingService
import no.novari.flyt.archive.gateway.dispatch.model.instance.DokumentbeskrivelseDto
import no.novari.flyt.archive.gateway.dispatch.model.instance.DokumentobjektDto
import no.novari.flyt.archive.gateway.dispatch.model.instance.JournalpostDto
import no.novari.flyt.archive.gateway.dispatch.web.CreatedLocationPollTimeoutException
import no.novari.flyt.archive.gateway.dispatch.web.FintArchiveDispatchClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class RecordDispatchService(
    private val journalpostMappingService: JournalpostMappingService,
    private val filesDispatchService: FilesDispatchService,
    private val fintArchiveDispatchClient: FintArchiveDispatchClient,
) {
    fun dispatch(
        caseId: String,
        journalpostDto: JournalpostDto,
    ): Mono<RecordDispatchResult> {
        log.info("Dispatching record")

        val dokumentobjektDtos = journalpostDto.dokumentbeskrivelse?.flatMap(this::getDokumentObjektDtos).orEmpty()
        if (dokumentobjektDtos.isEmpty()) {
            return dispatch(caseId, journalpostDto, emptyMap())
        }

        return filesDispatchService
            .dispatch(dokumentobjektDtos)
            .flatMap { filesDispatchResult ->
                when (filesDispatchResult.status) {
                    no.novari.flyt.archive.gateway.dispatch.DispatchStatus.ACCEPTED -> {
                        dispatch(caseId, journalpostDto, filesDispatchResult.archiveFileLinkPerFileId.orEmpty())
                    }

                    no.novari.flyt.archive.gateway.dispatch.DispatchStatus.DECLINED -> {
                        Mono.just(
                            RecordDispatchResult.declined(
                                "Dokumentobjekt declined by destination with message='${filesDispatchResult.errorMessage}'",
                            ),
                        )
                    }

                    no.novari.flyt.archive.gateway.dispatch.DispatchStatus.FAILED -> {
                        Mono.just(RecordDispatchResult.failed("Dokumentobjekt dispatch failed"))
                    }
                }
            }.doOnNext { result -> log.info("Dispatch result={}", result.toString()) }
    }

    private fun getDokumentObjektDtos(dokumentbeskrivelseDto: DokumentbeskrivelseDto): Collection<DokumentobjektDto> =
        dokumentbeskrivelseDto.dokumentobjekt.orEmpty()

    private fun dispatch(
        caseId: String,
        journalpostDto: JournalpostDto,
        archiveFileLinkPerFileId: Map<UUID, Link>,
    ): Mono<RecordDispatchResult> {
        val journalpostResource: JournalpostResource =
            journalpostMappingService.toJournalpostResource(journalpostDto, archiveFileLinkPerFileId)

        return fintArchiveDispatchClient
            .postRecord(caseId, journalpostResource)
            .map(JournalpostResource::getJournalPostnummer)
            .map(RecordDispatchResult::accepted)
            .onErrorResume(WebClientResponseException::class.java) { error ->
                Mono.just(RecordDispatchResult.declined(error.responseBodyAsString))
            }.onErrorResume({ error ->
                error is ReadTimeoutException || error is CreatedLocationPollTimeoutException
            }) { error ->
                log.error("Record dispatch timed out", error)
                Mono.just(RecordDispatchResult.timedOut())
            }.onErrorResume { error ->
                log.error("Failed to post record", error)
                Mono.just(RecordDispatchResult.failed("Failed to post record"))
            }
    }

    companion object {
        private val log = LoggerFactory.getLogger(RecordDispatchService::class.java)
    }
}
