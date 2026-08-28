package no.novari.flyt.archive.gateway.dispatch.mapping

class InvalidDokumentetsDatoException(
    value: String,
) : IllegalArgumentException(
        "Invalid dokumentetsDato '$value'. Expected ISO 8601 dateTime like yyyy-MM-dd'T'HH:mm:ssZ, " +
            "or date yyyy-MM-dd.",
    )
