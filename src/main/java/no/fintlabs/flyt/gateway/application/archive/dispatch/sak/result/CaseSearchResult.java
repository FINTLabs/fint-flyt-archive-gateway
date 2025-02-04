package no.fintlabs.flyt.gateway.application.archive.dispatch.sak.result;

import lombok.*;
import no.fintlabs.flyt.gateway.application.archive.dispatch.DispatchStatus;

import java.util.ArrayList;
import java.util.List;

@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CaseSearchResult {

    public static CaseSearchResult accepted(List<String> archiveCaseIds) {
        return new CaseSearchResult(DispatchStatus.ACCEPTED, archiveCaseIds != null ? archiveCaseIds : new ArrayList<>(), null);
    }

    public static CaseSearchResult declined(String errorMessage) {
        return new CaseSearchResult(DispatchStatus.DECLINED, null, errorMessage);
    }

    public static CaseSearchResult failed() {
        return new CaseSearchResult(DispatchStatus.FAILED, null, null);
    }

    public static CaseSearchResult timedOut() {
        return new CaseSearchResult(DispatchStatus.FAILED, null,
                "Case search timed out. No response from destination."
        );
    }

    private final DispatchStatus status;
    private final List<String> archiveCaseIds;
    private final String errorMessage;

}
