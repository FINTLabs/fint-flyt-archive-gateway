package no.novari.flyt.archive.gateway.dispatch.web;

public class CreatedLocationPollTimeoutException extends RuntimeException {

    public CreatedLocationPollTimeoutException() {
        super("Reached max total timeout for polling created location from destination");
    }

}
