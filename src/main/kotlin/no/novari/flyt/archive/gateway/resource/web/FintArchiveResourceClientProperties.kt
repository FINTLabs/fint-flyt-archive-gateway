package no.novari.flyt.archive.gateway.resource.web

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties("novari.flyt.archive.gateway.resource.fint-client")
class FintArchiveResourceClientProperties {
    var findCasesWithFilterMaxAttempts: Long? = null
    var findCasesWithFilterBackoffRetryMinDelay: Duration? = null
    var findCasesWithFilterBackoffRetryMaxDelay: Duration? = null
}
