package no.novari.flyt.archive.gateway.dispatch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DispatchMessageFormattingServiceTest {

    DispatchMessageFormattingService dispatchMessageFormattingService;

    @BeforeEach
    public void setup() {
        dispatchMessageFormattingService = new DispatchMessageFormattingService();
    }

    @Test
    public void givenEmptyRefsShouldReturnOptionalEmpty() {
        assertThat(dispatchMessageFormattingService.createFunctionalWarningMessage(
                "testObjectDisplayName",
                "testRefDisplayName",
                List.of()
        )).isEmpty();
    }

    @Test
    public void givenSingleRefShouldFormatMessageForSingleRef() {
        assertThat(dispatchMessageFormattingService.createFunctionalWarningMessage(
                "testObjectDisplayName",
                "testRefDisplayName",
                List.of("ref1")
        )).contains("testObjectDisplayName with testRefDisplayName='ref1'");
    }

    @Test
    public void givenMultipleRefsShouldFormatMessageForMultipleRefs() {
        assertThat(dispatchMessageFormattingService.createFunctionalWarningMessage(
                "testObjectDisplayName",
                "testRefDisplayName",
                List.of("ref1", "ref2", "ref3")
        )).contains("testObjectDisplayNames with testRefDisplayNames=['ref1', 'ref2', 'ref3']");
    }

    @Test
    public void givenCaseIdWithJournalpostNumbers_whenFormattingCaseIdAndJournalpostIds_thenReturnCaseIdWithJournalpostIds() {
        assertThat(dispatchMessageFormattingService.formatCaseIdAndJournalpostIds(
                "testCaseId",
                List.of(1L)
        )).contains("testCaseId-[1]");
    }

    @Test
    public void givenCaseIdWithNoJournalpostNumbers_whenFormattingCaseIdAndJournalpostIds_thenReturnCaseIdWithNoJournalpostIds() {
        assertThat(dispatchMessageFormattingService.formatCaseIdAndJournalpostIds(
                "testCaseId",
                List.of()
        )).contains("testCaseId");
    }

    @Test
    public void givenCaseIdAndNewCaseTrueAndListOfFunctionalWarningMessages_whenCombiningFunctionalWarningMessages_thenReturnMessageWithCaseIdAndFunctionalWarningMessages() {
        assertThat(dispatchMessageFormattingService.combineFunctionalWarningMessages(
                "testCaseId",
                true,
                List.of("test warning message")
        )).contains("(!) Already successfully dispatched sak with id=testCaseId, test warning message (!)");
    }

    @Test
    public void givenCaseIdAndNewCaseFalseAndListOfFunctionalWarningMessages_whenCombiningFunctionalWarningMessages_thenReturnMessageWithCaseIdAndFunctionalWarningMessages() {
        assertThat(dispatchMessageFormattingService.combineFunctionalWarningMessages(
                "testCaseId",
                false,
                List.of("test warning message")
        )).contains("(!) Already successfully dispatched test warning message (!)");
    }

    @Test
    public void givenCaseIdAndNewCaseTrueAndEmptyListOfFunctionalWarningMessages_whenCombiningFunctionalWarningMessages_thenReturnMessageWithCaseIdAndFunctionalWarningMessages() {
        assertThat(dispatchMessageFormattingService.combineFunctionalWarningMessages(
                "testCaseId",
                true,
                List.of()
        )).contains("(!) Already successfully dispatched sak with id=testCaseId (!)");
    }

    @Test
    public void givenCaseIdAndNewCaseFalseAndEmptyListOfFunctionalWarningMessages_whenCombiningFunctionalWarningMessages_thenReturnMessageWithCaseIdAndFunctionalWarningMessages() {
        assertThat(dispatchMessageFormattingService.combineFunctionalWarningMessages(
                "testCaseId",
                false,
                List.of()
        )).isEmpty();
    }

}
