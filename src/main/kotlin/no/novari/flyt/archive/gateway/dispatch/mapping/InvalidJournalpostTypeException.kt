package no.novari.flyt.archive.gateway.dispatch.mapping

class InvalidJournalpostTypeException(
    journalposttype: String,
) : IllegalArgumentException(
        "Ukjent journalposttype='$journalposttype'. Verdien må være en self-lenke fra arkivets kodeverk " +
            "for journalposttype. Korriger mappingen og send instansen på nytt.",
    )
