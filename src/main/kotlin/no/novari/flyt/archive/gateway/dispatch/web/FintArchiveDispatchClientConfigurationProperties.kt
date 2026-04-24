package no.novari.flyt.archive.gateway.dispatch.web

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties("novari.flyt.archive.gateway.dispatch.fint-client")
class FintArchiveDispatchClientConfigurationProperties {
    var postFileTimeout: Duration? = null
    var postCaseTimeout: Duration? = null
    var postRecordTimeout: Duration? = null
    var getStatusTimeout: Duration? = null
    var createdLocationPollTotalTimeout: Duration? = null
    var createdLocationPollBackoffMinDelay: Duration? = null
    var createdLocationPollBackoffMaxDelay: Duration? = null
}
