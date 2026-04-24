package no.novari.flyt.archive.gateway.resource.sak

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
class CaseController(
    private val caseRequestService: CaseRequestService,
) {
    @GetMapping("{caseYear}/{caseNumber}/tittel")
    fun getCaseTitle(
        @PathVariable caseYear: String,
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
