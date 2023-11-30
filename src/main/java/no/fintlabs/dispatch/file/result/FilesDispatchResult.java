package no.fintlabs.dispatch.file.result;

import lombok.*;
import no.fint.model.resource.Link;
import no.fintlabs.dispatch.DispatchStatus;

import java.util.Map;
import java.util.UUID;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
@ToString
public class FilesDispatchResult {

    public static FilesDispatchResult accepted(
            Map<UUID, Link> archiveFileLinkPerFileId
    ) {
        return new FilesDispatchResult(DispatchStatus.ACCEPTED, archiveFileLinkPerFileId, null);
    }

    public static FilesDispatchResult declined(String errorMessage) {
        return new FilesDispatchResult(DispatchStatus.DECLINED, null, errorMessage);
    }

    public static FilesDispatchResult failed() {
        return new FilesDispatchResult(DispatchStatus.FAILED, null, null);
    }

    private final DispatchStatus status;
    private final Map<UUID, Link> archiveFileLinkPerFileId;
    private final String errorMessage;

}
