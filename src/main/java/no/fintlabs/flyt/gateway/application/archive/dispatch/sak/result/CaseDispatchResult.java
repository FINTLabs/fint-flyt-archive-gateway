package no.fintlabs.flyt.gateway.application.archive.dispatch.sak.result;

import lombok.*;
import no.fintlabs.flyt.gateway.application.archive.dispatch.DispatchStatus;

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
