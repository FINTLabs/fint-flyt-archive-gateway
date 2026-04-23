package no.novari.flyt.archive.gateway.dispatch.web

import java.net.URI
import java.time.Duration

class CreatedLocationPollTimeoutException : RuntimeException {
    constructor() : super("Reached max total timeout for polling created location from destination")

    constructor(
        statusUri: URI,
        totalTimeout: Duration,
    ) : super(
        "Reached max total timeout for polling created location from destination: statusUri=$statusUri totalTimeout=$totalTimeout",
    )

    constructor(
        statusUri: URI,
        totalTimeout: Duration,
        lastStatus: String?,
    ) : super(
        "Reached max total timeout for polling created location from destination: " +
            "statusUri=$statusUri totalTimeout=$totalTimeout lastStatus=$lastStatus",
    )
}
