package no.novari.flyt.archive.gateway.template

import no.novari.flyt.archive.gateway.template.model.ValueTemplate
import org.springframework.stereotype.Service

@Service
class DokumentetsDatoTemplateService {
    fun createTemplate(): ValueTemplate =
        ValueTemplate
            .builder()
            .type(ValueTemplate.Type.DYNAMIC_STRING)
            .build()
}
