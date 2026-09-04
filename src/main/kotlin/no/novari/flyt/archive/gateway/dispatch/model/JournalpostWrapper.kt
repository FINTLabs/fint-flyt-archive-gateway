package no.novari.flyt.archive.gateway.dispatch.model

import com.fasterxml.jackson.databind.JsonNode

data class JournalpostWrapper(
    val journalpost: Collection<JsonNode>,
)
