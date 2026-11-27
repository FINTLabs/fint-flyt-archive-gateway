package no.novari.flyt.archive.gateway.resource.kodeverk

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
import no.novari.flyt.cache.FlytCache
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.Locale

@Configuration
class ResourceCacheConfiguration {
    @Bean
    fun administrativEnhetResourceCache(): FlytCache<String, AdministrativEnhetResource> =
        createCache(AdministrativEnhetResource::class.java)

    @Bean
    fun arkivdelResourceCache(): FlytCache<String, ArkivdelResource> = createCache(ArkivdelResource::class.java)

    @Bean
    fun arkivressursResourceCache(): FlytCache<String, ArkivressursResource> =
        createCache(ArkivressursResource::class.java)

    @Bean
    fun tilknyttetRegistreringSomResourceCache(): FlytCache<String, TilknyttetRegistreringSomResource> =
        createCache(TilknyttetRegistreringSomResource::class.java)

    @Bean
    fun dokumentStatusResourceCache(): FlytCache<String, DokumentStatusResource> =
        createCache(DokumentStatusResource::class.java)

    @Bean
    fun dokumentTypeResourceCache(): FlytCache<String, DokumentTypeResource> =
        createCache(DokumentTypeResource::class.java)

    @Bean
    fun klassifikasjonssystemResourceCache(): FlytCache<String, KlassifikasjonssystemResource> =
        createCache(KlassifikasjonssystemResource::class.java)

    @Bean
    fun partRolleResourceCache(): FlytCache<String, PartRolleResource> = createCache(PartRolleResource::class.java)

    @Bean
    fun korrespondansepartTypeResourceCache(): FlytCache<String, KorrespondansepartTypeResource> =
        createCache(KorrespondansepartTypeResource::class.java)

    @Bean
    fun saksstatusResourceCache(): FlytCache<String, SaksstatusResource> = createCache(SaksstatusResource::class.java)

    @Bean
    fun skjermingshjemmelResourceCache(): FlytCache<String, SkjermingshjemmelResource> =
        createCache(SkjermingshjemmelResource::class.java)

    @Bean
    fun tilgangsrestriksjonResourceCache(): FlytCache<String, TilgangsrestriksjonResource> =
        createCache(TilgangsrestriksjonResource::class.java)

    @Bean
    fun journalStatusResourceCache(): FlytCache<String, JournalStatusResource> =
        createCache(JournalStatusResource::class.java)

    @Bean
    fun journalpostTypeResourceCache(): FlytCache<String, JournalpostTypeResource> =
        createCache(JournalpostTypeResource::class.java)

    @Bean
    fun saksmappetypeResourceCache(): FlytCache<String, SaksmappetypeResource> =
        createCache(SaksmappetypeResource::class.java)

    @Bean
    fun variantformatResourceCache(): FlytCache<String, VariantformatResource> =
        createCache(VariantformatResource::class.java)

    @Bean
    fun formatResourceCache(): FlytCache<String, FormatResource> = createCache(FormatResource::class.java)

    @Bean
    fun tilgangsgruppeResourceCache(): FlytCache<String, TilgangsgruppeResource> =
        createCache(TilgangsgruppeResource::class.java)

    @Bean
    fun personalressursResourceCache(): FlytCache<String, PersonalressursResource> =
        createCache(PersonalressursResource::class.java)

    @Bean
    fun personResourceCache(): FlytCache<String, PersonResource> = createCache(PersonResource::class.java)

    private fun <V : Any> createCache(resourceClass: Class<V>): FlytCache<String, V> =
        FlytCache(resourceClass.name.lowercase(Locale.ROOT))
}
