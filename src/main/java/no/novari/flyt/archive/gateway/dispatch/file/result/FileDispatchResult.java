package no.novari.flyt.archive.gateway.dispatch.file.result;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import no.fint.model.resource.Link;
import no.novari.flyt.archive.gateway.dispatch.DispatchStatus;

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

    public static FileDispatchResult timedOut(UUID fileId) {
        return new FileDispatchResult(DispatchStatus.FAILED, fileId, null,
                "File dispatch timed out. No response from destination."
        );
    }

    private final DispatchStatus status;
    private final UUID fileId;
    private final Link archiveFileLink;
    private final String errorMessage;

}
