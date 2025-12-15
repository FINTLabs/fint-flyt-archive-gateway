package no.novari.flyt.archive.gateway.dispatch.sak.result;

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
public class CaseDispatchResult {

    public static CaseDispatchResult accepted(String archiveCaseId) {
        return new CaseDispatchResult(DispatchStatus.ACCEPTED, archiveCaseId, null);
    }

    public static CaseDispatchResult declined(String errorMessage) {
        return new CaseDispatchResult(DispatchStatus.DECLINED, null, errorMessage);
    }

    public static CaseDispatchResult failed() {
        return new CaseDispatchResult(DispatchStatus.FAILED, null, null);
    }

    public static CaseDispatchResult timedOut() {
        return new CaseDispatchResult(DispatchStatus.FAILED, null,
                "Case dispatch timed out. No response from destination.");
    }

    private final DispatchStatus status;
    private final String archiveCaseId;
    private final String errorMessage;

}
