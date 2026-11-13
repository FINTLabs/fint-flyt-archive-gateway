package no.novari.flyt.archive.gateway.resource.sak

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.BAD_REQUEST)
class InvalidArchiveInstanceIdException(
    archiveInstanceId: String,
) : RuntimeException("Invalid archiveInstanceId=$archiveInstanceId")
