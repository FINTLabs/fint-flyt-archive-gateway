package no.novari.flyt.archive.gateway

import no.novari.flyt.archive.gateway.slack.SlackAlertService
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.concurrent.Executor

class WebUtilErrorHandlerTest {
    private lateinit var slackAlertService: SlackAlertService
    private lateinit var executor: Executor
    private lateinit var handler: WebUtilErrorHandler

    @BeforeEach
    fun setup() {
        slackAlertService = mock()
        executor = Executor { runnable -> runnable.run() }
        handler = WebUtilErrorHandler(slackAlertService, executor)
    }

    @Test
    fun `given an error, dispatches the Slack message via the executor`() {
        handler.logAndSendError(RuntimeException("boom"))

        verify(slackAlertService, timeout(1000)).sendMessage("java.lang.RuntimeException: boom")
    }

    @Test
    fun `given a failing Slack call, does not propagate the exception`() {
        whenever(slackAlertService.sendMessage(org.mockito.kotlin.any()))
            .thenThrow(RuntimeException("slack down"))

        assertThatCode { handler.logAndSendError(RuntimeException("boom")) }
            .doesNotThrowAnyException()
    }
}
