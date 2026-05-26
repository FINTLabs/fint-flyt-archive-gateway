package no.novari.flyt.archive.gateway.dispatch.web.flytfile

import no.novari.flyt.archive.gateway.dispatch.model.File
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import java.time.Duration
import java.util.UUID

@Service
class FlytFileClient(
    @param:Qualifier("fileRestClient")
    private val fileRestClient: RestClient,
) {
    fun getFile(fileId: UUID): File {
        log.info("Getting file")
        var attempt = 0
        var delay = INITIAL_RETRY_DELAY
        while (true) {
            try {
                val file =
                    requireNotNull(
                        fileRestClient
                            .get()
                            .uri("/$fileId")
                            .retrieve()
                            .body<File>(),
                    ) { "Empty response body when retrieving file with id=$fileId" }
                log.info("Retrieved file with id={}", fileId)
                return file
            } catch (error: Throwable) {
                if (attempt >= MAX_RETRIES) {
                    log.error("Could not retrieve file with id={}", fileId, error)
                    throw error
                }
                attempt++
                log.warn(
                    "Could not retrieve file with id={} -- performing retry {}",
                    fileId,
                    attempt,
                    error,
                )
                Thread.sleep(delay.toMillis())
                delay = delay.multipliedBy(2)
            }
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(FlytFileClient::class.java)
        private const val MAX_RETRIES = 5
        private val INITIAL_RETRY_DELAY: Duration = Duration.ofSeconds(1)
    }
}
