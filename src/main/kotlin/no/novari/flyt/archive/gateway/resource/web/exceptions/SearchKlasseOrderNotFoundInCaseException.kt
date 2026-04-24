package no.novari.flyt.archive.gateway.resource.web.exceptions

class SearchKlasseOrderNotFoundInCaseException(
    caseKlasseOrders: List<Int>,
    searchKlasseOrder: Int?,
) : RuntimeException(
        "Could not find search klasse order=$searchKlasseOrder in case klasse orders=$caseKlasseOrders",
    )
