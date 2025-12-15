package no.novari.flyt.archive.gateway.resource.sak;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.arkiv.noark.SakResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static no.novari.flyt.resourceserver.UrlPaths.INTERNAL_API;

@Slf4j
@RestController
@RequestMapping(INTERNAL_API + "/arkiv/saker")
public class CaseController {

    private final CaseRequestService caseRequestService;

    public CaseController(
            CaseRequestService caseRequestService
    ) {
        this.caseRequestService = caseRequestService;
    }

    @GetMapping("{caseYear}/{caseNumber}/tittel")
    public ResponseEntity<CaseTitle> getCaseTitle(@PathVariable String caseYear, @PathVariable String caseNumber) {
        String mappeId = caseYear + "/" + caseNumber;
        return caseRequestService.getByMappeId(mappeId)
                .map(SakResource::getTittel)
                .map(CaseTitle::new)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Case with mappeId=%s could not be found", mappeId)
                ));
    }

}
