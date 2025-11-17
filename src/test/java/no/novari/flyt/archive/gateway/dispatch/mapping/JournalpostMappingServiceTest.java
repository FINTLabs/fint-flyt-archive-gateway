package no.novari.flyt.archive.gateway.dispatch.mapping;

import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.JournalpostResource;
import no.fint.model.resource.arkiv.noark.SkjermingResource;
import no.novari.flyt.archive.gateway.dispatch.model.instance.DokumentbeskrivelseDto;
import no.novari.flyt.archive.gateway.dispatch.model.instance.JournalpostDto;
import no.novari.flyt.archive.gateway.dispatch.model.instance.KorrespondansepartDto;
import no.novari.flyt.archive.gateway.dispatch.model.instance.SkjermingDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
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

        assertThat(mappedResource.getTittel()).isEqualTo("Tittel");
        assertThat(mappedResource.getOffentligTittel()).isEqualTo("Offentlig tittel");
        assertThat(mappedResource.getJournalposttype().getFirst().getHref()).isEqualTo("Journalpost Type");
        assertThat(mappedResource.getAdministrativEnhet().getFirst().getHref()).isEqualTo("Administrativ enhet");
        assertThat(mappedResource.getSaksbehandler().getFirst().getHref()).isEqualTo("Saksbehandler");
        assertThat(mappedResource.getJournalstatus().getFirst().getHref()).isEqualTo("Journalstatus");
        assertThat(mappedResource.getTilgangsgruppe().getFirst().getHref()).isEqualTo("Tilgangsgruppe");

        assertThat(mappedResource.getSkjerming().getTilgangsrestriksjon())
                .anyMatch(link -> "Tilgangsrestriksjon".equals(link.getHref()));
        assertThat(mappedResource.getSkjerming().getSkjermingshjemmel())
                .anyMatch(link -> "Skjermingshjemmel".equals(link.getHref()));
    }

}
