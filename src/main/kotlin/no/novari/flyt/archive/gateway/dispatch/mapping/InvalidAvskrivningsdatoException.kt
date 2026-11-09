package no.novari.flyt.archive.gateway.dispatch.mapping

class InvalidAvskrivningsdatoException(
    avskrivningsdato: String,
) : IllegalArgumentException(
        "Ugyldig avskrivningsdato='$avskrivningsdato'. Feltet må være et ISO 8601-tidsstempel med dato, " +
            "klokkeslett med sekunder og tidssone (Z eller +/-hh:mm). Millisekunder er tillatt, for eksempel " +
            "2026-08-24T11:12:48.123+02:00. Korriger verdien og send instansen på nytt.",
    )
