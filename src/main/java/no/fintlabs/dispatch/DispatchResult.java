package no.fintlabs.dispatch;

import lombok.*;

@Getter
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class DispatchResult {

    public static DispatchResult accepted(String archiveCaseId) {
        return new DispatchResult(DispatchStatus.ACCEPTED, archiveCaseId, null);
    }

    public static DispatchResult declined(String errorMessage) {
        return new DispatchResult(DispatchStatus.DECLINED, null, errorMessage);
    }

    public static DispatchResult failed() {
        return DispatchResult.failed(null);
    }

    public static DispatchResult failed(String errorMessage) {
        return new DispatchResult(DispatchStatus.FAILED, null, errorMessage);
    }

    private final DispatchStatus status;
    private final String archiveCaseAndRecordsIds;
    private final String errorMessage;

}
