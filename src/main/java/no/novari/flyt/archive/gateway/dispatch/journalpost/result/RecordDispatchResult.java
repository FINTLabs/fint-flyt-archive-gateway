package no.novari.flyt.archive.gateway.dispatch.journalpost.result;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import no.novari.flyt.archive.gateway.dispatch.DispatchStatus;

@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RecordDispatchResult {

    public static RecordDispatchResult accepted(Long journalpostId) {
        return new RecordDispatchResult(DispatchStatus.ACCEPTED, journalpostId, null);
    }

    public static RecordDispatchResult declined(String errorMessage) {
        return new RecordDispatchResult(DispatchStatus.DECLINED, null, errorMessage);
    }

    public static RecordDispatchResult failed(String errorMessage) {
        return new RecordDispatchResult(DispatchStatus.FAILED, null, errorMessage);
    }

    public static RecordDispatchResult timedOut() {
        return new RecordDispatchResult(DispatchStatus.FAILED, null,
                "Record dispatch timed out. No response from destination."
        );
    }

    private final DispatchStatus status;
    private final Long journalpostId;
    private final String errorMessage;


}
