package no.novari.flyt.archive.gateway.resource.sak

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import no.novari.fint.model.resource.arkiv.noark.SakResource
import no.novari.flyt.webresourceserver.UrlPaths.INTERNAL_CLIENT_API
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("$INTERNAL_CLIENT_API/arkiv/saker")
@Tag(name = "Saker (intern klient)", description = "Oppslag mot arkivsaker for andre FLYT-tjenester.")
class InternalClientCaseController(
    private val caseService: CaseService,
) {
    @GetMapping("{caseYear}/{caseNumber}")
    @Operation(summary = "Hent sak", description = "Henter sak på mappeId på formatet saksår/saksnummer.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Sak funnet."),
            ApiResponse(responseCode = "404", description = "Fant ingen sak med oppgitt mappeId."),
        ],
    )
    fun getCase(
        @Parameter(description = "Saksår i arkivet.", example = "2024")
        @PathVariable caseYear: String,
        @Parameter(description = "Saksnummer i arkivet.", example = "123")
        @PathVariable caseNumber: String,
    ): SakResource {
        val mappeId = "$caseYear/$caseNumber"
        return caseService.findByMappeId(mappeId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Sak with mappeId=$mappeId could not be found")
    }

    @GetMapping(params = ["archiveInstanceId"])
    @Operation(
        summary = "Hent sak med filtrerte journalposter",
        description = "Henter sak på archiveInstanceId, med bare journalpostene som archiveInstanceId peker på.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Sak funnet."),
            ApiResponse(responseCode = "400", description = "Ugyldig archiveInstanceId."),
            ApiResponse(responseCode = "404", description = "Fant ingen sak for oppgitt archiveInstanceId."),
        ],
    )
    fun getCaseWithFilteredJournalposts(
        @Parameter(
            description = "Arkivinstans-id på formatet saksår/saksnummer-[journalpostnumre].",
            example = "2024/123-[1,2]",
        )
        @RequestParam archiveInstanceId: String,
    ): SakResource =
        caseService.findByArchiveInstanceIdWithFilteredJournalposts(archiveInstanceId)
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Sak with archiveInstanceId=$archiveInstanceId could not be found",
            )
}
