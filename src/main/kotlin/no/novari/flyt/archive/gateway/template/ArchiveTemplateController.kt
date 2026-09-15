package no.novari.flyt.archive.gateway.template

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import no.novari.flyt.archive.gateway.template.model.MappingTemplate
import no.novari.flyt.webresourceserver.UrlPaths.INTERNAL_API
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("$INTERNAL_API/arkiv/template")
@Tag(name = "Arkivmal", description = "Mappingmal som Flyt bruker ved arkivering.")
class ArchiveTemplateController(
    private val archiveTemplateService: ArchiveTemplateService,
) {
    @GetMapping
    @Operation(
        summary = "Hent arkivmal",
        description = "Returnerer mappingmalen for arkivinstanser, inkludert felter, valg og avhengigheter.",
    )
    fun getTemplate(): MappingTemplate = archiveTemplateService.createTemplate()
}
