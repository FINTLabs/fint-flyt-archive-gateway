package no.novari.flyt.archive.gateway.dispatch.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import no.fint.model.resource.arkiv.noark.JournalpostResource;

import java.util.Collection;
import java.util.List;

@Getter
@EqualsAndHashCode
public class JournalpostWrapper {

    public JournalpostWrapper(JournalpostResource journalpost) {
        this.journalpost = List.of(journalpost);
    }

    private final Collection<JournalpostResource> journalpost;

}
