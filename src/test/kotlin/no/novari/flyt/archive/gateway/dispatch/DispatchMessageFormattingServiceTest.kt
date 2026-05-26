package no.novari.flyt.archive.gateway.dispatch

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DispatchMessageFormattingServiceTest {
    private lateinit var dispatchMessageFormattingService: DispatchMessageFormattingService

    @BeforeEach
    fun setup() {
        dispatchMessageFormattingService = DispatchMessageFormattingService()
    }

    @Test
    fun `given empty refs, returns empty optional`() {
        assertThat(
            dispatchMessageFormattingService.createFunctionalWarningMessage(
                "testObjectDisplayName",
                "testRefDisplayName",
                emptyList(),
            ),
        ).isEmpty
    }

    @Test
    fun `given a single ref, formats the message for a single ref`() {
        assertThat(
            dispatchMessageFormattingService.createFunctionalWarningMessage(
                "testObjectDisplayName",
                "testRefDisplayName",
                listOf("ref1"),
            ),
        ).contains("testObjectDisplayName with testRefDisplayName='ref1'")
    }

    @Test
    fun `given multiple refs, formats the message for multiple refs`() {
        assertThat(
            dispatchMessageFormattingService.createFunctionalWarningMessage(
                "testObjectDisplayName",
                "testRefDisplayName",
                listOf("ref1", "ref2", "ref3"),
            ),
        ).contains("testObjectDisplayNames with testRefDisplayNames=['ref1', 'ref2', 'ref3']")
    }

    @Test
    fun `given a case id with journalpost numbers, formats it as case id with journalpost ids`() {
        assertThat(dispatchMessageFormattingService.formatCaseIdAndJournalpostIds("testCaseId", listOf(1L)))
            .isEqualTo("testCaseId-[1]")
    }

    @Test
    fun `given a case id without journalpost numbers, returns only the case id`() {
        assertThat(dispatchMessageFormattingService.formatCaseIdAndJournalpostIds("testCaseId", emptyList()))
            .isEqualTo("testCaseId")
    }

    @Test
    fun `given a new case and warning messages, includes the case id and warnings`() {
        assertThat(
            dispatchMessageFormattingService.combineFunctionalWarningMessages(
                "testCaseId",
                true,
                listOf("test warning message"),
            ),
        ).contains("(!) Already successfully dispatched sak with id=testCaseId, test warning message (!)")
    }

    @Test
    fun `given an existing case and warning messages, only includes the warnings`() {
        assertThat(
            dispatchMessageFormattingService.combineFunctionalWarningMessages(
                "testCaseId",
                false,
                listOf("test warning message"),
            ),
        ).contains("(!) Already successfully dispatched test warning message (!)")
    }

    @Test
    fun `given a new case and no warnings, returns the case message`() {
        assertThat(
            dispatchMessageFormattingService.combineFunctionalWarningMessages(
                "testCaseId",
                true,
                emptyList(),
            ),
        ).contains("(!) Already successfully dispatched sak with id=testCaseId (!)")
    }

    @Test
    fun `given an existing case and no warnings, returns empty optional`() {
        assertThat(
            dispatchMessageFormattingService.combineFunctionalWarningMessages(
                "testCaseId",
                false,
                emptyList(),
            ),
        ).isEmpty
    }
}
