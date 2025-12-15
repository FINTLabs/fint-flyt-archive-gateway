package no.novari.flyt.archive.gateway.resource.configuration;

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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ResourcePipelineConfiguration {

    private final FintLinksResourcePipelineFactory fintLinksResourcePipelineFactory;

    public ResourcePipelineConfiguration(FintLinksResourcePipelineFactory fintLinksResourcePipelineFactory) {
        this.fintLinksResourcePipelineFactory = fintLinksResourcePipelineFactory;
    }

    @Bean
    public ResourcePipeline<AdministrativEnhetResource> administrativEnhetResourcePipeline(
            FintCache<String, AdministrativEnhetResource> administrativEnhetResourceCache
    ) {
        return fintLinksResourcePipelineFactory.createResourcePipeLine(
                AdministrativEnhetResource.class,
                administrativEnhetResourceCache,
                List.of("arkiv", "noark", "administrativenhet"),
                true
        );
    }

    @Bean
    public ResourcePipeline<KlassifikasjonssystemResource> klassifikasjonssystemResourcePipeline(
            FintCache<String, KlassifikasjonssystemResource> klassifikasjonssystemResourceCache
    ) {
        return fintLinksResourcePipelineFactory.createResourcePipeLine(
                KlassifikasjonssystemResource.class,
                klassifikasjonssystemResourceCache,
                List.of("arkiv", "noark", "klassifikasjonssystem"),
                false
        );
    }

    @Bean
    public ResourcePipeline<PartRolleResource> partRolleResourcePipeline(
            FintCache<String, PartRolleResource> partRolleResourceCache
    ) {
        return fintLinksResourcePipelineFactory.createResourcePipeLine(
                PartRolleResource.class,
                partRolleResourceCache,
                List.of("arkiv", "kodeverk", "partrolle"),
                true
        );
    }

    @Bean
    public ResourcePipeline<KorrespondansepartTypeResource> korrespondansepartTypeResourcePipeline(
            FintCache<String, KorrespondansepartTypeResource> korrespondansepartTypeResourceCache
    ) {
        return fintLinksResourcePipelineFactory.createResourcePipeLine(
                KorrespondansepartTypeResource.class,
                korrespondansepartTypeResourceCache,
                List.of("arkiv", "kodeverk", "korrespondanseparttype"),
                true
        );
    }

    @Bean
    public ResourcePipeline<SaksstatusResource> saksstatusResourcePipeline(
            FintCache<String, SaksstatusResource> saksstatusResourceCache
    ) {
        return fintLinksResourcePipelineFactory.createResourcePipeLine(
                SaksstatusResource.class,
                saksstatusResourceCache,
                List.of("arkiv", "kodeverk", "saksstatus"),
                true
        );
    }

    @Bean
    public ResourcePipeline<ArkivdelResource> arkivdelResourcePipeline(
            FintCache<String, ArkivdelResource> arkivdelResourceCache
    ) {
        return fintLinksResourcePipelineFactory.createResourcePipeLine(
                ArkivdelResource.class,
                arkivdelResourceCache,
                List.of("arkiv", "noark", "arkivdel"),
                true
        );
    }

    @Bean
    public ResourcePipeline<SkjermingshjemmelResource> skjermingshjemmelResourcePipeline(
            FintCache<String, SkjermingshjemmelResource> stringSkjermingshjemmelResourceCache
    ) {
        return fintLinksResourcePipelineFactory.createResourcePipeLine(
                SkjermingshjemmelResource.class,
                stringSkjermingshjemmelResourceCache,
                List.of("arkiv", "kodeverk", "skjermingshjemmel"),
                true
        );
    }

    @Bean
    public ResourcePipeline<TilgangsrestriksjonResource> tilgangsrestriksjonResourcePipeline(
            FintCache<String, TilgangsrestriksjonResource> tilgangsrestriksjonResourceCache
    ) {
        return fintLinksResourcePipelineFactory.createResourcePipeLine(
                TilgangsrestriksjonResource.class,
                tilgangsrestriksjonResourceCache,
                List.of("arkiv", "kodeverk", "tilgangsrestriksjon"),
                true
        );
    }

    @Bean
    public ResourcePipeline<DokumentStatusResource> dokumentStatusResourcePipeline(
            FintCache<String, DokumentStatusResource> dokumentStatusResourceCache
    ) {
        return fintLinksResourcePipelineFactory.createResourcePipeLine(
                DokumentStatusResource.class,
                dokumentStatusResourceCache,
                List.of("arkiv", "kodeverk", "dokumentstatus"),
                true
        );
    }

    @Bean
    public ResourcePipeline<DokumentTypeResource> dokumentTypeResourcePipeline(
            FintCache<String, DokumentTypeResource> dokumentTypeResourceCache
    ) {
        return fintLinksResourcePipelineFactory.createResourcePipeLine(
                DokumentTypeResource.class,
                dokumentTypeResourceCache,
                List.of("arkiv", "kodeverk", "dokumenttype"),
                true
        );
    }

    @Bean
    public ResourcePipeline<JournalpostTypeResource> journalpostTypeResourcePipeline(
            FintCache<String, JournalpostTypeResource> journalpostTypeResourceCache
    ) {
        return fintLinksResourcePipelineFactory.createResourcePipeLine(
                JournalpostTypeResource.class,
                journalpostTypeResourceCache,
                List.of("arkiv", "kodeverk", "journalposttype"),
                true
        );
    }

    @Bean
    public ResourcePipeline<JournalStatusResource> journalStatusResourcePipeline(
            FintCache<String, JournalStatusResource> journalStatusResourceCache
    ) {
        return fintLinksResourcePipelineFactory.createResourcePipeLine(
                JournalStatusResource.class,
                journalStatusResourceCache,
                List.of("arkiv", "kodeverk", "journalstatus"),
                true
        );
    }

    @Bean
    public ResourcePipeline<VariantformatResource> variantformatResourcePipeline(
            FintCache<String, VariantformatResource> variantformatResourceCache
    ) {
        return fintLinksResourcePipelineFactory.createResourcePipeLine(
                VariantformatResource.class,
                variantformatResourceCache,
                List.of("arkiv", "kodeverk", "variantformat"),
                true
        );
    }

    @Bean
    public ResourcePipeline<FormatResource> formatResourcePipeline(
            FintCache<String, FormatResource> formatResourceCache
    ) {
        return fintLinksResourcePipelineFactory.createResourcePipeLine(
                FormatResource.class,
                formatResourceCache,
                List.of("arkiv", "kodeverk", "format"),
                true
        );
    }

    @Bean
    public ResourcePipeline<TilgangsgruppeResource> tilgangsgruppeResourcePipeline(
            FintCache<String, TilgangsgruppeResource> tilgangsgruppeResourceCache
    ) {
        return fintLinksResourcePipelineFactory.createResourcePipeLine(
                TilgangsgruppeResource.class,
                tilgangsgruppeResourceCache,
                List.of("arkiv", "kodeverk", "tilgangsgruppe"),
                true
        );
    }

    @Bean
    public ResourcePipeline<SaksmappetypeResource> saksmappetypeResourcePipeline(
            FintCache<String, SaksmappetypeResource> saksmappetypeResourceCache
    ) {
        return fintLinksResourcePipelineFactory.createResourcePipeLine(
                SaksmappetypeResource.class,
                saksmappetypeResourceCache,
                List.of("arkiv", "kodeverk", "saksmappetype"),
                true
        );
    }

    @Bean
    public ResourcePipeline<TilknyttetRegistreringSomResource> tilknyttetRegistreringSomResourcePipeline(
            FintCache<String, TilknyttetRegistreringSomResource> tilknyttetRegistreringSomResourceCache
    ) {
        return fintLinksResourcePipelineFactory.createResourcePipeLine(
                TilknyttetRegistreringSomResource.class,
                tilknyttetRegistreringSomResourceCache,
                List.of("arkiv", "kodeverk", "tilknyttetregistreringsom"),
                true
        );
    }

    @Bean
    public ResourcePipeline<ArkivressursResource> arkivressursResourcePipeline(
            FintCache<String, ArkivressursResource> arkivressursResourceCache
    ) {
        return fintLinksResourcePipelineFactory.createResourcePipeLine(
                ArkivressursResource.class,
                arkivressursResourceCache,
                List.of("arkiv", "noark", "arkivressurs"),
                true
        );
    }

    @Bean
    public ResourcePipeline<PersonalressursResource> personalressursResourcePipeline(
            FintCache<String, PersonalressursResource> personalressursResourceCache
    ) {
        return fintLinksResourcePipelineFactory.createResourcePipeLine(
                PersonalressursResource.class,
                personalressursResourceCache,
                List.of("administrasjon", "personal", "personalressurs"),
                true
        );
    }

    @Bean
    public ResourcePipeline<PersonResource> personResourcePipeline(
            FintCache<String, PersonResource> personResourceCache
    ) {
        return fintLinksResourcePipelineFactory.createResourcePipeLine(
                PersonResource.class,
                personResourceCache,
                List.of("administrasjon", "personal", "person"),
                true
        );
    }
}
