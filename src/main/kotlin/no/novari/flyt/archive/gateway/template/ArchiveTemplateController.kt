package no.novari.flyt.archive.gateway.template

import no.novari.flyt.archive.gateway.template.model.MappingTemplate
import no.novari.flyt.webresourceserver.UrlPaths.INTERNAL_API
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("$INTERNAL_API/arkiv/template")
class ArchiveTemplateController(
    private val archiveTemplateService: ArchiveTemplateService,
) {
    @GetMapping
    fun getTemplate(): MappingTemplate = archiveTemplateService.createTemplate()
}
