package no.novari.flyt.archive.gateway.links

import no.novari.fint.model.resource.FintLinks

class NoSuchLinkException(
    message: String,
) : RuntimeException(message) {
    companion object {
        @JvmStatic
        fun noSelfLink(resource: FintLinks) = NoSuchLinkException("No self link in resource=$resource")

        @JvmStatic
        fun noLink(
            resource: FintLinks,
            linkedResourceName: String,
        ) = NoSuchLinkException("No link for '$linkedResourceName' in resource=$resource")
    }
}
