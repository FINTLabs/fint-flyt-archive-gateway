package no.novari.flyt.archive.gateway.dispatch.mapping;

import no.fint.model.resource.felles.kompleksedatatyper.AdresseResource;
import no.novari.flyt.archive.gateway.dispatch.model.instance.AdresseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

class AdresseMappingServiceTest {

    private AdresseMappingService adresseMappingService;

    @BeforeEach
    void setUp() {
        adresseMappingService = new AdresseMappingService();
    }

    @Test
    void toAdresseResource() {
        Collection<String> adresseLinjer = Arrays.asList("Linje 1", "Linje 2");
        String postNummer = "12345";
        String postSted = "testPostSted";

        AdresseDto adresseDto = AdresseDto.builder()
                .adresselinje(adresseLinjer)
                .postnummer(postNummer)
                .poststed(postSted)
                .build();

        AdresseResource adresseResource = adresseMappingService.toAdresseResource(adresseDto);

        assertThat(adresseResource).isNotNull();
        assertThat(adresseResource.getAdresselinje()).containsExactlyElementsOf(adresseLinjer);
        assertThat(adresseResource.getPostnummer()).isEqualTo(postNummer);
        assertThat(adresseResource.getPoststed()).isEqualTo(postSted);
    }

    @Test
    void toAdresseResource_NullFields() {
        AdresseDto adresseDto = AdresseDto.builder().build();

        AdresseResource adresseResource = adresseMappingService.toAdresseResource(adresseDto);

        assertThat(adresseResource).isNotNull();
        assertThat(adresseResource.getAdresselinje()).isNull();
        assertThat(adresseResource.getPostnummer()).isNull();
        assertThat(adresseResource.getPoststed()).isNull();
    }
}
