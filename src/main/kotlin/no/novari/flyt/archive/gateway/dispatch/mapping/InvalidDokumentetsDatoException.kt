package no.novari.flyt.archive.gateway.dispatch.mapping

class InvalidDokumentetsDatoException(
    dokumentetsDato: String,
) : IllegalArgumentException(
        "Invalid dokumentetsDato='$dokumentetsDato'. Expected ISO 8601 date " +
            "'yyyy-MM-dd' or date-time 'yyyy-MM-dd'T'HH:mm:ssZ'.",
    )
