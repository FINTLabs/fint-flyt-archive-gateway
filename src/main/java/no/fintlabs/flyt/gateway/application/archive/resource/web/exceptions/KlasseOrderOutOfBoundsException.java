package no.fintlabs.flyt.gateway.application.archive.resource.web.exceptions;

public class KlasseOrderOutOfBoundsException extends RuntimeException {
    public KlasseOrderOutOfBoundsException(
            String errorMessage
    ) {
        super(errorMessage);
    }
}
