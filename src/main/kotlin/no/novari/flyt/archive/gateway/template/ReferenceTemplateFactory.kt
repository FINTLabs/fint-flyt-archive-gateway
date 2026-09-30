package no.novari.flyt.archive.gateway.template

import no.novari.fint.model.FintModelObject
import no.novari.flyt.archive.gateway.template.model.ReferenceTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Locale
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

@Component
class ReferenceTemplateFactory(
    @Value("\${novari.flyt.archive.gateway.client.fint-archive.base-url}")
    baseUrl: String,
) {
    private val baseUrl = baseUrl.trimEnd('/')

    fun create(resourceClass: KClass<out FintModelObject>): ReferenceTemplate =
        ReferenceTemplate(
            urlTemplate = "$baseUrl/${resourcePath(resourceClass)}/$IDENTIFIER_TYPE/$IDENTIFIER_VALUE",
            identifierTypes = identifierTypes(resourceClass),
        )

    private fun resourcePath(resourceClass: KClass<out FintModelObject>): String {
        val componentPath =
            resourceClass.java.packageName
                .removePrefix(FINT_MODEL_PACKAGE_PREFIX)
                .replace('.', '/')
        return "$componentPath/${resourceClass.java.simpleName.lowercase(Locale.ROOT)}"
    }

    // FINT self-links use lowercase identifier types, e.g. .../arkivressurs/systemid/123
    private fun identifierTypes(resourceClass: KClass<out FintModelObject>): List<String> =
        resourceClass
            .createInstance()
            .identifikators
            .keys
            .map { it.lowercase(Locale.ROOT) }

    companion object {
        const val IDENTIFIER_TYPE = "{identifierType}"
        const val IDENTIFIER_VALUE = "{identifierValue}"
        private const val FINT_MODEL_PACKAGE_PREFIX = "no.novari.fint.model."
    }
}
