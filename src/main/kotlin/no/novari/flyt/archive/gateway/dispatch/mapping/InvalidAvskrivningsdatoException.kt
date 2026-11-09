package no.novari.flyt.archive.gateway.dispatch.mapping

class InvalidAvskrivningsdatoException(
    avskrivningsdato: String,
) : IllegalArgumentException(
        "Ugyldig avskrivningsdato='$avskrivningsdato'. Feltet må være på formatet " +
            "YYYY-MM-DDThh:mm:ssZ (UTC). Korriger verdien og send instansen på nytt.",
    )
