package no.novari.flyt.archive.gateway.resource.sak

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import no.novari.fint.model.resource.arkiv.noark.SakResource
import no.novari.flyt.webresourceserver.UrlPaths.INTERNAL_API
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("$INTERNAL_API/arkiv/saker")
@Tag(name = "Saker", description = "Oppslag mot arkivsaker.")
class CaseController(
    private val caseRequestService: CaseRequestService,
) {
    @GetMapping("{caseYear}/{caseNumber}/tittel")
    @Operation(
        summary = "Hent sakstittel",
        description = "Henter tittel for en sak ved å slå opp mappeId på formatet saksår/saksnummer.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Sakstittel funnet."),
            ApiResponse(responseCode = "404", description = "Fant ingen sak med oppgitt mappeId."),
        ],
    )
    fun getCaseTitle(
        @Parameter(description = "Saksår i arkivet.", example = "2024")
        @PathVariable caseYear: String,
        @Parameter(description = "Saksnummer i arkivet.", example = "123")
        @PathVariable caseNumber: String,
    ): ResponseEntity<CaseTitle> {
        val mappeId = "$caseYear/$caseNumber"
        val title =
            caseRequestService.getByMappeId(mappeId)?.let(SakResource::getTittel)
                ?: throw ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Case with mappeId=$mappeId could not be found",
                )

        return ResponseEntity.ok(CaseTitle(title))
    }
}
