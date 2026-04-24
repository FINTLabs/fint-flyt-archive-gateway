package no.novari.flyt.archive.gateway.links

import no.novari.fint.model.resource.FintLinks
import no.novari.fint.model.resource.Link
import java.util.function.Supplier

object ResourceLinkUtil {
    @JvmStatic
    fun getFirstSelfLink(resource: FintLinks): String =
        resource.selfLinks
            .firstOrNull()
            ?.href
            ?: throw NoSuchLinkException.noSelfLink(resource)

    @JvmStatic
    fun getSelfLinks(resource: FintLinks): List<String> = resource.selfLinks.map(Link::getHref)

    @JvmStatic
    fun getFirstLink(
        linkProducer: Supplier<List<Link>?>,
        resource: FintLinks,
        linkedResourceName: String,
    ): String =
        linkProducer
            .get()
            ?.firstOrNull()
            ?.href
            ?: throw NoSuchLinkException.noLink(resource, linkedResourceName)
}
