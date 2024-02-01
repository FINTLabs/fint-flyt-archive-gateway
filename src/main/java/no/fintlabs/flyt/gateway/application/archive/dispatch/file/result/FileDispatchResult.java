package no.fintlabs.flyt.gateway.application.archive.dispatch.file.result;

import lombok.*;
import no.fint.model.resource.Link;
import no.fintlabs.flyt.gateway.application.archive.dispatch.DispatchStatus;

import java.util.UUID;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
@ToString
public class FileDispatchResult {

    public static FileDispatchResult accepted(UUID fileId, Link archiveFileLink) {
        return new FileDispatchResult(DispatchStatus.ACCEPTED, fileId, archiveFileLink, null);
    }

    public static FileDispatchResult declined(UUID fileId, String errorMessage) {
        return new FileDispatchResult(DispatchStatus.DECLINED, fileId, null, errorMessage);
    }

    public static FileDispatchResult couldNotBeRetrieved(UUID fileId) {
        return new FileDispatchResult(DispatchStatus.FAILED, fileId, null, "Could not retrieve file");
    }

    public static FileDispatchResult noFileId() {
        return new FileDispatchResult(DispatchStatus.FAILED, null, null, "No fileId");
    }

    public static FileDispatchResult failed(UUID fileId) {
        return new FileDispatchResult(DispatchStatus.FAILED, fileId, null, null);
    }

    private final DispatchStatus status;
    private final UUID fileId;
    private final Link archiveFileLink;
    private final String errorMessage;

}
