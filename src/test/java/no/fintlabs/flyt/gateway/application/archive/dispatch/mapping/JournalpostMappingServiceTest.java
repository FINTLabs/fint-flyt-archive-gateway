package no.fintlabs.flyt.gateway.application.archive.dispatch.mapping;

import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.JournalpostResource;
import no.fint.model.resource.arkiv.noark.SkjermingResource;
import no.fintlabs.flyt.gateway.application.archive.dispatch.model.instance.DokumentbeskrivelseDto;
import no.fintlabs.flyt.gateway.application.archive.dispatch.model.instance.JournalpostDto;
import no.fintlabs.flyt.gateway.application.archive.dispatch.model.instance.KorrespondansepartDto;
import no.fintlabs.flyt.gateway.application.archive.dispatch.model.instance.SkjermingDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class JournalpostMappingServiceTest {

    private JournalpostMappingService journalpostMappingService;

    @Mock
    private SkjermingMappingService skjermingMappingService;

    @Mock
    private KorrespondansepartMappingService korrespondanseparMappingService;

    @Mock
    private DokumentbeskrivelseMappingService dokumentbeskrivelseMappingService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);


        journalpostMappingService = new JournalpostMappingService(
                skjermingMappingService,
                korrespondanseparMappingService,
                dokumentbeskrivelseMappingService
        );

        when(skjermingMappingService.toSkjermingResource(any(SkjermingDto.class))).thenAnswer(invocation -> {
            SkjermingDto dto = invocation.getArgument(0);
            SkjermingResource resource = new SkjermingResource();
            dto.getTilgangsrestriksjon().map(Link::with).ifPresent(resource::addTilgangsrestriksjon);
            dto.getSkjermingshjemmel().map(Link::with).ifPresent(resource::addSkjermingshjemmel);
            return resource;
        });
    }

    @Test
    public void testToJournalpostResource() {

        SkjermingDto skjermingDto = SkjermingDto.builder()
                .tilgangsrestriksjon("Tilgangsrestriksjon")
                .skjermingshjemmel("Skjermingshjemmel")
                .build();

        KorrespondansepartDto korrespondansepartDto = KorrespondansepartDto.builder()
                .korrespondanseparttype("Korrespondansepart Type")
                .organisasjonsnummer("Organisasjonsnummer")
                .fodselsnummer("Fodselsnummer")
                .korrespondansepartNavn("Korrespondansepart Navn")
                .kontaktperson("Kontaktperson")
                .skjerming(skjermingDto)
                .build();

        DokumentbeskrivelseDto dokumentbeskrivelseDto = DokumentbeskrivelseDto.builder()
                .tittel("Dokumentbeskrivelse Tittel")
                .dokumentstatus("Dokumentstatus")
                .dokumentType("Dokument Type")
                .tilknyttetRegistreringSom("Tilknyttet Registrering Som")
                .skjerming(skjermingDto)
                .build();

        JournalpostDto dto = JournalpostDto
                .builder()
                .tittel("Tittel")
                .offentligTittel("Offentlig tittel")
                .journalposttype("Journalpost Type")
                .administrativEnhet("Administrativ enhet")
                .saksbehandler("Saksbehandler")
                .journalstatus("Journalstatus")
                .tilgangsgruppe("Tilgangsgruppe")
                .skjerming(skjermingDto)
                .korrespondansepart(Collections.singletonList(korrespondansepartDto))
                .dokumentbeskrivelse(Collections.singletonList(dokumentbeskrivelseDto))
                .build();
        Map<UUID, Link> fileArchiveLinkPerFileId = new HashMap<>();

        JournalpostResource mappedResource = journalpostMappingService.toJournalpostResource(dto, fileArchiveLinkPerFileId);

        assertEquals("Tittel", mappedResource.getTittel());
        assertEquals("Offentlig tittel", mappedResource.getOffentligTittel());
        assertEquals("Journalpost Type", mappedResource.getJournalposttype().iterator().next().getHref());
        assertEquals("Administrativ enhet", mappedResource.getAdministrativEnhet().iterator().next().getHref());
        assertEquals("Saksbehandler", mappedResource.getSaksbehandler().iterator().next().getHref());
        assertEquals("Journalstatus", mappedResource.getJournalstatus().iterator().next().getHref());
        assertEquals("Tilgangsgruppe", mappedResource.getTilgangsgruppe().iterator().next().getHref());

        assertTrue(mappedResource.getSkjerming().getTilgangsrestriksjon().stream().anyMatch(link -> "Tilgangsrestriksjon".equals(link.getHref())));
        assertTrue(mappedResource.getSkjerming().getSkjermingshjemmel().stream().anyMatch(link -> "Skjermingshjemmel".equals(link.getHref())));
    }

}
