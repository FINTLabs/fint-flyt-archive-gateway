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
    fun givenEmptyRefsShouldReturnOptionalEmpty() {
        assertThat(
            dispatchMessageFormattingService.createFunctionalWarningMessage(
                "testObjectDisplayName",
                "testRefDisplayName",
                emptyList(),
            ),
        ).isEmpty
    }

    @Test
    fun givenSingleRefShouldFormatMessageForSingleRef() {
        assertThat(
            dispatchMessageFormattingService.createFunctionalWarningMessage(
                "testObjectDisplayName",
                "testRefDisplayName",
                listOf("ref1"),
            ),
        ).contains("testObjectDisplayName with testRefDisplayName='ref1'")
    }

    @Test
    fun givenMultipleRefsShouldFormatMessageForMultipleRefs() {
        assertThat(
            dispatchMessageFormattingService.createFunctionalWarningMessage(
                "testObjectDisplayName",
                "testRefDisplayName",
                listOf("ref1", "ref2", "ref3"),
            ),
        ).contains("testObjectDisplayNames with testRefDisplayNames=['ref1', 'ref2', 'ref3']")
    }

    @Test
    fun givenCaseIdWithJournalpostNumbersWhenFormattingThenReturnCaseIdWithJournalpostIds() {
        assertThat(dispatchMessageFormattingService.formatCaseIdAndJournalpostIds("testCaseId", listOf(1L)))
            .isEqualTo("testCaseId-[1]")
    }

    @Test
    fun givenCaseIdWithNoJournalpostNumbersWhenFormattingThenReturnCaseId() {
        assertThat(dispatchMessageFormattingService.formatCaseIdAndJournalpostIds("testCaseId", emptyList()))
            .isEqualTo("testCaseId")
    }

    @Test
    fun givenNewCaseAndWarningMessagesWhenCombiningThenIncludeCaseIdAndWarnings() {
        assertThat(
            dispatchMessageFormattingService.combineFunctionalWarningMessages(
                "testCaseId",
                true,
                listOf("test warning message"),
            ),
        ).contains("(!) Already successfully dispatched sak with id=testCaseId, test warning message (!)")
    }

    @Test
    fun givenExistingCaseAndWarningMessagesWhenCombiningThenOnlyIncludeWarnings() {
        assertThat(
            dispatchMessageFormattingService.combineFunctionalWarningMessages(
                "testCaseId",
                false,
                listOf("test warning message"),
            ),
        ).contains("(!) Already successfully dispatched test warning message (!)")
    }

    @Test
    fun givenNewCaseAndNoWarningsWhenCombiningThenReturnCaseMessage() {
        assertThat(
            dispatchMessageFormattingService.combineFunctionalWarningMessages(
                "testCaseId",
                true,
                emptyList(),
            ),
        ).contains("(!) Already successfully dispatched sak with id=testCaseId (!)")
    }

    @Test
    fun givenExistingCaseAndNoWarningsWhenCombiningThenReturnEmptyOptional() {
        assertThat(
            dispatchMessageFormattingService.combineFunctionalWarningMessages(
                "testCaseId",
                false,
                emptyList(),
            ),
        ).isEmpty
    }
}
