package no.novari.flyt.archive.gateway.dispatch.mapping

class InvalidDokumentetsDatoException(
    dokumentetsDato: String,
) : IllegalArgumentException(
        "Ugyldig dokumentetsDato='$dokumentetsDato'. Feltet må være på ISO 8601-format " +
            "YYYY-MM-DDThh:mm:ssZ. Korriger verdien og send instansen på nytt.",
    )
