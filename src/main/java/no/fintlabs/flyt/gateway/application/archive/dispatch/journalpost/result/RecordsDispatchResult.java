package no.fintlabs.flyt.gateway.application.archive.dispatch.journalpost.result;

import lombok.*;
import no.fintlabs.flyt.gateway.application.archive.dispatch.DispatchStatus;

import java.util.List;

@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RecordsDispatchResult {

    public static RecordsDispatchResult accepted(List<Long> journalpostIds) {
        return new RecordsDispatchResult(DispatchStatus.ACCEPTED, journalpostIds, null, null);
    }

    public static RecordsDispatchResult declined(
            String errorMessage,
            String functionalWarningMessage
    ) {
        return new RecordsDispatchResult(DispatchStatus.DECLINED, null, errorMessage, functionalWarningMessage);
    }

    public static RecordsDispatchResult failed(
            String errorMessage,
            String functionalWarningMessage
    ) {
        return new RecordsDispatchResult(DispatchStatus.FAILED, null, errorMessage, functionalWarningMessage);
    }

    private final DispatchStatus status;
    private final List<Long> journalpostIds;
    private final String errorMessage;
    private final String functionalWarningMessage;

}
