package no.novari.flyt.archive.gateway.dispatch.model

import no.novari.fint.model.resource.arkiv.noark.JournalpostResource

data class JournalpostWrapper(
    val journalpost: Collection<JournalpostResource>,
) {
    constructor(journalpost: JournalpostResource) : this(listOf(journalpost))
}
