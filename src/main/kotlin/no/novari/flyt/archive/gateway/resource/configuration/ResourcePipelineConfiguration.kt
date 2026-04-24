package no.novari.flyt.archive.gateway.resource.configuration

import no.novari.cache.FintCache
import no.novari.fint.model.resource.administrasjon.personal.PersonalressursResource
import no.novari.fint.model.resource.arkiv.kodeverk.DokumentStatusResource
import no.novari.fint.model.resource.arkiv.kodeverk.DokumentTypeResource
import no.novari.fint.model.resource.arkiv.kodeverk.FormatResource
import no.novari.fint.model.resource.arkiv.kodeverk.JournalStatusResource
import no.novari.fint.model.resource.arkiv.kodeverk.JournalpostTypeResource
import no.novari.fint.model.resource.arkiv.kodeverk.KorrespondansepartTypeResource
import no.novari.fint.model.resource.arkiv.kodeverk.PartRolleResource
import no.novari.fint.model.resource.arkiv.kodeverk.SaksmappetypeResource
import no.novari.fint.model.resource.arkiv.kodeverk.SaksstatusResource
import no.novari.fint.model.resource.arkiv.kodeverk.SkjermingshjemmelResource
import no.novari.fint.model.resource.arkiv.kodeverk.TilgangsgruppeResource
import no.novari.fint.model.resource.arkiv.kodeverk.TilgangsrestriksjonResource
import no.novari.fint.model.resource.arkiv.kodeverk.TilknyttetRegistreringSomResource
import no.novari.fint.model.resource.arkiv.kodeverk.VariantformatResource
import no.novari.fint.model.resource.arkiv.noark.AdministrativEnhetResource
import no.novari.fint.model.resource.arkiv.noark.ArkivdelResource
import no.novari.fint.model.resource.arkiv.noark.ArkivressursResource
import no.novari.fint.model.resource.arkiv.noark.KlassifikasjonssystemResource
import no.novari.fint.model.resource.felles.PersonResource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ResourcePipelineConfiguration(
    private val fintLinksResourcePipelineFactory: FintLinksResourcePipelineFactory,
) {
    @Bean
    fun administrativEnhetResourcePipeline(
        administrativEnhetResourceCache: FintCache<String, AdministrativEnhetResource>,
    ) = fintLinksResourcePipelineFactory.createResourcePipeLine(
        AdministrativEnhetResource::class.java,
        administrativEnhetResourceCache,
        listOf("arkiv", "noark", "administrativenhet"),
        true,
    )

    @Bean
    fun klassifikasjonssystemResourcePipeline(
        klassifikasjonssystemResourceCache: FintCache<String, KlassifikasjonssystemResource>,
    ) = fintLinksResourcePipelineFactory.createResourcePipeLine(
        KlassifikasjonssystemResource::class.java,
        klassifikasjonssystemResourceCache,
        listOf("arkiv", "noark", "klassifikasjonssystem"),
        false,
    )

    @Bean
    fun partRolleResourcePipeline(partRolleResourceCache: FintCache<String, PartRolleResource>) =
        fintLinksResourcePipelineFactory.createResourcePipeLine(
            PartRolleResource::class.java,
            partRolleResourceCache,
            listOf("arkiv", "kodeverk", "partrolle"),
            true,
        )

    @Bean
    fun korrespondansepartTypeResourcePipeline(
        korrespondansepartTypeResourceCache: FintCache<String, KorrespondansepartTypeResource>,
    ) = fintLinksResourcePipelineFactory.createResourcePipeLine(
        KorrespondansepartTypeResource::class.java,
        korrespondansepartTypeResourceCache,
        listOf("arkiv", "kodeverk", "korrespondanseparttype"),
        true,
    )

    @Bean
    fun saksstatusResourcePipeline(saksstatusResourceCache: FintCache<String, SaksstatusResource>) =
        fintLinksResourcePipelineFactory.createResourcePipeLine(
            SaksstatusResource::class.java,
            saksstatusResourceCache,
            listOf("arkiv", "kodeverk", "saksstatus"),
            true,
        )

    @Bean
    fun arkivdelResourcePipeline(arkivdelResourceCache: FintCache<String, ArkivdelResource>) =
        fintLinksResourcePipelineFactory.createResourcePipeLine(
            ArkivdelResource::class.java,
            arkivdelResourceCache,
            listOf("arkiv", "noark", "arkivdel"),
            true,
        )

    @Bean
    fun skjermingshjemmelResourcePipeline(
        skjermingshjemmelResourceCache: FintCache<String, SkjermingshjemmelResource>,
    ) = fintLinksResourcePipelineFactory.createResourcePipeLine(
        SkjermingshjemmelResource::class.java,
        skjermingshjemmelResourceCache,
        listOf("arkiv", "kodeverk", "skjermingshjemmel"),
        true,
    )

    @Bean
    fun tilgangsrestriksjonResourcePipeline(
        tilgangsrestriksjonResourceCache: FintCache<String, TilgangsrestriksjonResource>,
    ) = fintLinksResourcePipelineFactory.createResourcePipeLine(
        TilgangsrestriksjonResource::class.java,
        tilgangsrestriksjonResourceCache,
        listOf("arkiv", "kodeverk", "tilgangsrestriksjon"),
        true,
    )

    @Bean
    fun dokumentStatusResourcePipeline(dokumentStatusResourceCache: FintCache<String, DokumentStatusResource>) =
        fintLinksResourcePipelineFactory.createResourcePipeLine(
            DokumentStatusResource::class.java,
            dokumentStatusResourceCache,
            listOf("arkiv", "kodeverk", "dokumentstatus"),
            true,
        )

    @Bean
    fun dokumentTypeResourcePipeline(dokumentTypeResourceCache: FintCache<String, DokumentTypeResource>) =
        fintLinksResourcePipelineFactory.createResourcePipeLine(
            DokumentTypeResource::class.java,
            dokumentTypeResourceCache,
            listOf("arkiv", "kodeverk", "dokumenttype"),
            true,
        )

    @Bean
    fun journalpostTypeResourcePipeline(journalpostTypeResourceCache: FintCache<String, JournalpostTypeResource>) =
        fintLinksResourcePipelineFactory.createResourcePipeLine(
            JournalpostTypeResource::class.java,
            journalpostTypeResourceCache,
            listOf("arkiv", "kodeverk", "journalposttype"),
            true,
        )

    @Bean
    fun journalStatusResourcePipeline(journalStatusResourceCache: FintCache<String, JournalStatusResource>) =
        fintLinksResourcePipelineFactory.createResourcePipeLine(
            JournalStatusResource::class.java,
            journalStatusResourceCache,
            listOf("arkiv", "kodeverk", "journalstatus"),
            true,
        )

    @Bean
    fun variantformatResourcePipeline(variantformatResourceCache: FintCache<String, VariantformatResource>) =
        fintLinksResourcePipelineFactory.createResourcePipeLine(
            VariantformatResource::class.java,
            variantformatResourceCache,
            listOf("arkiv", "kodeverk", "variantformat"),
            true,
        )

    @Bean
    fun formatResourcePipeline(formatResourceCache: FintCache<String, FormatResource>) =
        fintLinksResourcePipelineFactory.createResourcePipeLine(
            FormatResource::class.java,
            formatResourceCache,
            listOf("arkiv", "kodeverk", "format"),
            true,
        )

    @Bean
    fun tilgangsgruppeResourcePipeline(tilgangsgruppeResourceCache: FintCache<String, TilgangsgruppeResource>) =
        fintLinksResourcePipelineFactory.createResourcePipeLine(
            TilgangsgruppeResource::class.java,
            tilgangsgruppeResourceCache,
            listOf("arkiv", "kodeverk", "tilgangsgruppe"),
            true,
        )

    @Bean
    fun saksmappetypeResourcePipeline(saksmappetypeResourceCache: FintCache<String, SaksmappetypeResource>) =
        fintLinksResourcePipelineFactory.createResourcePipeLine(
            SaksmappetypeResource::class.java,
            saksmappetypeResourceCache,
            listOf("arkiv", "kodeverk", "saksmappetype"),
            true,
        )

    @Bean
    fun tilknyttetRegistreringSomResourcePipeline(
        tilknyttetRegistreringSomResourceCache: FintCache<String, TilknyttetRegistreringSomResource>,
    ) = fintLinksResourcePipelineFactory.createResourcePipeLine(
        TilknyttetRegistreringSomResource::class.java,
        tilknyttetRegistreringSomResourceCache,
        listOf("arkiv", "kodeverk", "tilknyttetregistreringsom"),
        true,
    )

    @Bean
    fun arkivressursResourcePipeline(arkivressursResourceCache: FintCache<String, ArkivressursResource>) =
        fintLinksResourcePipelineFactory.createResourcePipeLine(
            ArkivressursResource::class.java,
            arkivressursResourceCache,
            listOf("arkiv", "noark", "arkivressurs"),
            true,
        )

    @Bean
    fun personalressursResourcePipeline(personalressursResourceCache: FintCache<String, PersonalressursResource>) =
        fintLinksResourcePipelineFactory.createResourcePipeLine(
            PersonalressursResource::class.java,
            personalressursResourceCache,
            listOf("administrasjon", "personal", "personalressurs"),
            true,
        )

    @Bean
    fun personResourcePipeline(personResourceCache: FintCache<String, PersonResource>) =
        fintLinksResourcePipelineFactory.createResourcePipeLine(
            PersonResource::class.java,
            personResourceCache,
            listOf("administrasjon", "personal", "person"),
            true,
        )
}
