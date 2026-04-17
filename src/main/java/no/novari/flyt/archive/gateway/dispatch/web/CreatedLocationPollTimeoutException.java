package no.novari.flyt.archive.gateway.dispatch.web;

import java.net.URI;
import java.time.Duration;

public class CreatedLocationPollTimeoutException extends RuntimeException {

    public CreatedLocationPollTimeoutException() {
        super("Reached max total timeout for polling created location from destination");
    }

    public CreatedLocationPollTimeoutException(URI statusUri, Duration totalTimeout) {
        super("Reached max total timeout for polling created location from destination: statusUri="
              + statusUri + " totalTimeout=" + totalTimeout);
    }

    public CreatedLocationPollTimeoutException(URI statusUri, Duration totalTimeout, String lastStatus) {
        super("Reached max total timeout for polling created location from destination: statusUri="
              + statusUri + " totalTimeout=" + totalTimeout + " lastStatus=" + lastStatus);
    }

}
