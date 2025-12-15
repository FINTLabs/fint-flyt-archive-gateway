package no.novari.flyt.archive.gateway.dispatch.mapping;

import no.fint.model.felles.kompleksedatatyper.Kontaktinformasjon;
import no.fint.model.resource.arkiv.noark.PartResource;
import no.fint.model.resource.felles.kompleksedatatyper.AdresseResource;
import no.novari.flyt.archive.gateway.dispatch.model.instance.AdresseDto;
import no.novari.flyt.archive.gateway.dispatch.model.instance.KontaktinformasjonDto;
import no.novari.flyt.archive.gateway.dispatch.model.instance.PartDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PartMappingServiceTest {

    @InjectMocks
    private PartMappingService partMappingService;

    @Mock
    private KontaktinformasjonMappingService kontaktinformasjonMappingService;

    @Mock
    private AdresseMappingService adresseMappingService;

    @Test
    public void testToPartResourceNullDto() {
        assertThat(partMappingService.toPartResource(null)).isNull();
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

        assertThat(partDto.getKontaktinformasjon()).isPresent();
        when(kontaktinformasjonMappingService.toKontaktinformasjon(partDto.getKontaktinformasjon().get()))
                .thenReturn(kontaktinformasjon);
        assertThat(partDto.getAdresse()).isPresent();
        when(adresseMappingService.toAdresseResource(partDto.getAdresse().get()))
                .thenReturn(adresseResource);

        PartResource partResource = partMappingService.toPartResource(partDto);

        assertThat(partResource.getPartNavn()).isEqualTo("Test Navn");
        assertThat(partResource.getPartRolle().getFirst().getHref()).isEqualTo("Test Rolle");
        assertThat(partResource.getKontaktperson()).isEqualTo("Test Person");
        assertThat(partResource.getFodselsnummer()).isEqualTo("123456789");
        assertThat(partResource.getOrganisasjonsnummer()).isEqualTo("987654321");
        assertThat(partResource.getAdresse()).isEqualTo(adresseResource);
        assertThat(partResource.getKontaktinformasjon()).isEqualTo(kontaktinformasjon);
    }
}
