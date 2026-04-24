package no.novari.flyt.archive.gateway.resource.web.exceptions

class KlasseOrderOutOfBoundsException(
    order: Int,
) : RuntimeException("Rekkefolge=$order is out of bounds. Rekkefolge must be 1, 2 or 3.")
