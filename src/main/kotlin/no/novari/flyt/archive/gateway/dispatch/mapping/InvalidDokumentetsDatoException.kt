package no.novari.flyt.archive.gateway.dispatch.mapping

class InvalidDokumentetsDatoException(
    dokumentetsDato: String,
) : IllegalArgumentException(
        "Ugyldig dokumentetsDato='$dokumentetsDato'. Feltet må være på formatet " +
            "YYYY-MM-DD. Korriger verdien og send instansen på nytt.",
    )
