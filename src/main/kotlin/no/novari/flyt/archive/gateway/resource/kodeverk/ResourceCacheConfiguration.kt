package no.novari.flyt.archive.gateway.resource.kodeverk

import no.novari.cache.FintCache
import no.novari.cache.FintCacheManager
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
import java.util.Locale

@Configuration
class ResourceCacheConfiguration(
    private val fintCacheManager: FintCacheManager,
) {
    @Bean
    fun administrativEnhetResourceCache(): FintCache<String, AdministrativEnhetResource> =
        createCache(AdministrativEnhetResource::class.java)

    @Bean
    fun arkivdelResourceCache(): FintCache<String, ArkivdelResource> = createCache(ArkivdelResource::class.java)

    @Bean
    fun arkivressursResourceCache(): FintCache<String, ArkivressursResource> =
        createCache(ArkivressursResource::class.java)

    @Bean
    fun tilknyttetRegistreringSomResourceCache(): FintCache<String, TilknyttetRegistreringSomResource> =
        createCache(TilknyttetRegistreringSomResource::class.java)

    @Bean
    fun dokumentStatusResourceCache(): FintCache<String, DokumentStatusResource> =
        createCache(DokumentStatusResource::class.java)

    @Bean
    fun dokumentTypeResourceCache(): FintCache<String, DokumentTypeResource> =
        createCache(DokumentTypeResource::class.java)

    @Bean
    fun klassifikasjonssystemResourceCache(): FintCache<String, KlassifikasjonssystemResource> =
        createCache(KlassifikasjonssystemResource::class.java)

    @Bean
    fun partRolleResourceCache(): FintCache<String, PartRolleResource> = createCache(PartRolleResource::class.java)

    @Bean
    fun korrespondansepartTypeResourceCache(): FintCache<String, KorrespondansepartTypeResource> =
        createCache(KorrespondansepartTypeResource::class.java)

    @Bean
    fun saksstatusResourceCache(): FintCache<String, SaksstatusResource> = createCache(SaksstatusResource::class.java)

    @Bean
    fun skjermingshjemmelResourceCache(): FintCache<String, SkjermingshjemmelResource> =
        createCache(SkjermingshjemmelResource::class.java)

    @Bean
    fun tilgangsrestriksjonResourceCache(): FintCache<String, TilgangsrestriksjonResource> =
        createCache(TilgangsrestriksjonResource::class.java)

    @Bean
    fun journalStatusResourceCache(): FintCache<String, JournalStatusResource> =
        createCache(JournalStatusResource::class.java)

    @Bean
    fun journalpostTypeResourceCache(): FintCache<String, JournalpostTypeResource> =
        createCache(JournalpostTypeResource::class.java)

    @Bean
    fun saksmappetypeResourceCache(): FintCache<String, SaksmappetypeResource> =
        createCache(SaksmappetypeResource::class.java)

    @Bean
    fun variantformatResourceCache(): FintCache<String, VariantformatResource> =
        createCache(VariantformatResource::class.java)

    @Bean
    fun formatResourceCache(): FintCache<String, FormatResource> = createCache(FormatResource::class.java)

    @Bean
    fun tilgangsgruppeResourceCache(): FintCache<String, TilgangsgruppeResource> =
        createCache(TilgangsgruppeResource::class.java)

    @Bean
    fun personalressursResourceCache(): FintCache<String, PersonalressursResource> =
        createCache(PersonalressursResource::class.java)

    @Bean
    fun personResourceCache(): FintCache<String, PersonResource> = createCache(PersonResource::class.java)

    private fun <V> createCache(resourceClass: Class<V>): FintCache<String, V> =
        fintCacheManager.createCache(
            resourceClass.name.lowercase(Locale.ROOT),
            String::class.java,
            resourceClass,
        )
}
