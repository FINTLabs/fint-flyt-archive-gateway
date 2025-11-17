package no.novari.flyt.archive.gateway.resource.kodeverk;

import no.fint.model.resource.administrasjon.personal.PersonalressursResource;
import no.fint.model.resource.arkiv.kodeverk.DokumentStatusResource;
import no.fint.model.resource.arkiv.kodeverk.DokumentTypeResource;
import no.fint.model.resource.arkiv.kodeverk.FormatResource;
import no.fint.model.resource.arkiv.kodeverk.JournalStatusResource;
import no.fint.model.resource.arkiv.kodeverk.JournalpostTypeResource;
import no.fint.model.resource.arkiv.kodeverk.KorrespondansepartTypeResource;
import no.fint.model.resource.arkiv.kodeverk.PartRolleResource;
import no.fint.model.resource.arkiv.kodeverk.SaksmappetypeResource;
import no.fint.model.resource.arkiv.kodeverk.SaksstatusResource;
import no.fint.model.resource.arkiv.kodeverk.SkjermingshjemmelResource;
import no.fint.model.resource.arkiv.kodeverk.TilgangsgruppeResource;
import no.fint.model.resource.arkiv.kodeverk.TilgangsrestriksjonResource;
import no.fint.model.resource.arkiv.kodeverk.TilknyttetRegistreringSomResource;
import no.fint.model.resource.arkiv.kodeverk.VariantformatResource;
import no.fint.model.resource.arkiv.noark.AdministrativEnhetResource;
import no.fint.model.resource.arkiv.noark.ArkivdelResource;
import no.fint.model.resource.arkiv.noark.ArkivressursResource;
import no.fint.model.resource.arkiv.noark.KlassifikasjonssystemResource;
import no.fint.model.resource.felles.PersonResource;
import no.novari.cache.FintCache;
import no.novari.cache.FintCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Locale;

@Configuration
public class ResourceCacheConfiguration {

    private final FintCacheManager fintCacheManager;

    public ResourceCacheConfiguration(FintCacheManager fintCacheManager) {
        this.fintCacheManager = fintCacheManager;
    }

    @Bean
    FintCache<String, AdministrativEnhetResource> administrativEnhetResourceCache() {
        return createCache(AdministrativEnhetResource.class);
    }

    @Bean
    FintCache<String, ArkivdelResource> arkivdelResourceCache() {
        return createCache(ArkivdelResource.class);
    }

    @Bean
    FintCache<String, ArkivressursResource> arkivressursResourceCache() {
        return createCache(ArkivressursResource.class);
    }

    @Bean
    FintCache<String, TilknyttetRegistreringSomResource> tilknyttetRegistreringSomResourceCache() {
        return createCache(TilknyttetRegistreringSomResource.class);
    }

    @Bean
    FintCache<String, DokumentStatusResource> dokumentStatusResourceCache() {
        return createCache(DokumentStatusResource.class);
    }

    @Bean
    FintCache<String, DokumentTypeResource> dokumentTypeResourceCache() {
        return createCache(DokumentTypeResource.class);
    }

    @Bean
    FintCache<String, KlassifikasjonssystemResource> klassifikasjonssystemResourceCache() {
        return createCache(KlassifikasjonssystemResource.class);
    }

    @Bean
    FintCache<String, PartRolleResource> partRolleResourceCache() {
        return createCache(PartRolleResource.class);
    }

    @Bean
    FintCache<String, KorrespondansepartTypeResource> korrespondansepartTypeResourceCache() {
        return createCache(KorrespondansepartTypeResource.class);
    }

    @Bean
    FintCache<String, SaksstatusResource> saksstatusResourceCache() {
        return createCache(SaksstatusResource.class);
    }

    @Bean
    FintCache<String, SkjermingshjemmelResource> skjermingshjemmelResourceCache() {
        return createCache(SkjermingshjemmelResource.class);
    }

    @Bean
    FintCache<String, TilgangsrestriksjonResource> tilgangsrestriksjonResourceCache() {
        return createCache(TilgangsrestriksjonResource.class);
    }

    @Bean
    FintCache<String, JournalStatusResource> journalStatusResourceCache() {
        return createCache(JournalStatusResource.class);
    }

    @Bean
    FintCache<String, JournalpostTypeResource> journalpostTypeResourceCache() {
        return createCache(JournalpostTypeResource.class);
    }

    @Bean
    FintCache<String, SaksmappetypeResource> saksmappetypeResourceCache() {
        return createCache(SaksmappetypeResource.class);
    }

    @Bean
    FintCache<String, VariantformatResource> variantformatResourceCache() {
        return createCache(VariantformatResource.class);
    }

    @Bean
    FintCache<String, FormatResource> formatResourceCache() {
        return createCache(FormatResource.class);
    }

    @Bean
    FintCache<String, TilgangsgruppeResource> tilgangsgruppeResourceCache() {
        return createCache(TilgangsgruppeResource.class);
    }

    @Bean
    FintCache<String, PersonalressursResource> personalressursResourceCache() {
        return createCache(PersonalressursResource.class);
    }

    @Bean
    FintCache<String, PersonResource> personResourceCache() {
        return createCache(PersonResource.class);
    }

    private <V> FintCache<String, V> createCache(Class<V> resourceClass) {
        return fintCacheManager.createCache(
                resourceClass.getName().toLowerCase(Locale.ROOT),
                String.class,
                resourceClass
        );
    }

}
