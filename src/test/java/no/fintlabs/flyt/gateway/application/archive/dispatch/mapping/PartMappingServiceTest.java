package no.fintlabs.flyt.gateway.application.archive.dispatch.mapping;

import no.fint.model.felles.kompleksedatatyper.Kontaktinformasjon;
import no.fint.model.resource.arkiv.noark.PartResource;
import no.fint.model.resource.felles.kompleksedatatyper.AdresseResource;
import no.fintlabs.flyt.gateway.application.archive.dispatch.model.instance.AdresseDto;
import no.fintlabs.flyt.gateway.application.archive.dispatch.model.instance.KontaktinformasjonDto;
import no.fintlabs.flyt.gateway.application.archive.dispatch.model.instance.PartDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

public class PartMappingServiceTest {

    @InjectMocks
    private PartMappingService partMappingService;

    @Mock
    private KontaktinformasjonMappingService kontaktinformasjonMappingService;

    @Mock
    private AdresseMappingService adresseMappingService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testToPartResourceNullDto() {
        assertNull(partMappingService.toPartResource(null));
    }

    @Test
    public void testToPartResource() {
        PartDto partDto = PartDto.builder()
                .partNavn("Test Navn")
                .partRolle("Test Rolle")
                .kontaktperson("Test Person")
                .fodselsnummer("123456789")
                .organisasjonsnummer("987654321")
                .adresse(AdresseDto.builder().adresselinje(new ArrayList<>()).postnummer("1234").poststed("Test Sted").build())
                .kontaktinformasjon(KontaktinformasjonDto.builder().epostadresse("test@test.com").telefonnummer("1234567890").mobiltelefonnummer("0987654321").build())
                .build();

        Kontaktinformasjon kontaktinformasjon = new Kontaktinformasjon();
        AdresseResource adresseResource = new AdresseResource();

        when(kontaktinformasjonMappingService.toKontaktinformasjon(partDto.getKontaktinformasjon().get()))
                .thenReturn(kontaktinformasjon);
        when(adresseMappingService.toAdresseResource(partDto.getAdresse().get()))
                .thenReturn(adresseResource);

        PartResource partResource = partMappingService.toPartResource(partDto);

        assertEquals("Test Navn", partResource.getPartNavn());
        assertEquals("Test Rolle", partResource.getPartRolle().get(0).getHref());
        assertEquals("Test Person", partResource.getKontaktperson());
        assertEquals("123456789", partResource.getFodselsnummer());
        assertEquals("987654321", partResource.getOrganisasjonsnummer());
        assertEquals(adresseResource, partResource.getAdresse());
        assertEquals(kontaktinformasjon, partResource.getKontaktinformasjon());
    }
}
