package no.novari.flyt.archive.gateway.dispatch.mapping

class JournalpostTypeLookupException(
    journalposttype: String,
    cause: Throwable? = null,
) : RuntimeException(
        "Kunne ikke fastslå kode for journalposttype='$journalposttype' fordi arkivets kodeverk er utilgjengelig " +
            "eller mangler data. Prøv å sende instansen på nytt når kodeverket er tilgjengelig.",
        cause,
    )
