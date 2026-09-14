package no.novari.flyt.archive.gateway.resource.kodeverk

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import no.novari.cache.FintCache
import no.novari.fint.model.felles.basisklasser.Begrep
import no.novari.fint.model.felles.kompleksedatatyper.Identifikator
import no.novari.fint.model.resource.FintLinks
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
import no.novari.fint.model.resource.arkiv.noark.KlasseResource
import no.novari.fint.model.resource.arkiv.noark.KlassifikasjonssystemResource
import no.novari.flyt.archive.gateway.links.ResourceLinkUtil
import no.novari.flyt.webresourceserver.UrlPaths.INTERNAL_API
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("$INTERNAL_API/arkiv/kodeverk")
@Tag(name = "Kodeverk", description = "Cachede arkivkodeverk og arkivressurser.")
class CodelistController(
    private val administrativEnhetResourceCache: FintCache<String, AdministrativEnhetResource>,
    private val arkivdelResourceCache: FintCache<String, ArkivdelResource>,
    private val arkivressursResourceCache: FintCache<String, ArkivressursResource>,
    private val tilknyttetRegistreringSomResourceCache: FintCache<String, TilknyttetRegistreringSomResource>,
    private val dokumentStatusResourceCache: FintCache<String, DokumentStatusResource>,
    private val dokumentTypeResourceCache: FintCache<String, DokumentTypeResource>,
    private val klassifikasjonssystemResourceCache: FintCache<String, KlassifikasjonssystemResource>,
    private val partRolleResourceCache: FintCache<String, PartRolleResource>,
    private val korrespondansepartTypeResourceCache: FintCache<String, KorrespondansepartTypeResource>,
    private val saksstatusResourceCache: FintCache<String, SaksstatusResource>,
    private val skjermingshjemmelResourceCache: FintCache<String, SkjermingshjemmelResource>,
    private val tilgangsrestriksjonResourceCache: FintCache<String, TilgangsrestriksjonResource>,
    private val journalStatusResourceCache: FintCache<String, JournalStatusResource>,
    private val journalpostTypeResourceCache: FintCache<String, JournalpostTypeResource>,
    private val saksmappetypeResourceCache: FintCache<String, SaksmappetypeResource>,
    private val variantformatResourceCache: FintCache<String, VariantformatResource>,
    private val formatResourceCache: FintCache<String, FormatResource>,
    private val tilgangsgruppeResourceCache: FintCache<String, TilgangsgruppeResource>,
    private val arkivressursDisplayNameMapper: ArkivressursDisplayNameMapper,
) {
    @GetMapping("administrativenhet")
    @Operation(summary = "List administrative enheter")
    fun getAdministrativEnheter(): ResponseEntity<Collection<ResourceReference>> =
        ResponseEntity.ok(
            administrativEnhetResourceCache
                .getAllDistinct()
                .map { administrativEnhetResource ->
                    mapToResourceReference(
                        administrativEnhetResource,
                        ResourceReferenceDisplayNameBuilder()
                            .technicalId(administrativEnhetResource.systemId)
                            .name(administrativEnhetResource.navn),
                    )
                },
        )

    @GetMapping("klassifikasjonssystem")
    @Operation(summary = "List klassifikasjonssystemer")
    fun getKlassifikasjonssystem(): ResponseEntity<Collection<ResourceReference>> =
        ResponseEntity.ok(
            klassifikasjonssystemResourceCache
                .getAllDistinct()
                .map { klassifikasjonssystemResource ->
                    mapToResourceReference(
                        klassifikasjonssystemResource,
                        ResourceReferenceDisplayNameBuilder()
                            .technicalId(klassifikasjonssystemResource.systemId)
                            .name(klassifikasjonssystemResource.tittel),
                    )
                },
        )

    @GetMapping("klasse")
    @Operation(
        summary = "List klasser for klassifikasjonssystem",
        description = "Returnerer klasser som hører til et gitt klassifikasjonssystem.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Klasser funnet."),
            ApiResponse(
                responseCode = "404",
                description = "Fant ikke klassifikasjonssystemet eller tilhørende klasser.",
            ),
        ],
    )
    fun getKlasse(
        @Parameter(description = "Self-link for klassifikasjonssystemet.", required = true)
        @RequestParam klassifikasjonssystemLink: String,
    ): ResponseEntity<Collection<ResourceReference>> {
        val klasseResources: List<KlasseResource> =
            klassifikasjonssystemResourceCache
                .getOptional(klassifikasjonssystemLink)
                .orElse(null)
                ?.klasse
                ?.toList()
                ?: emptyList()
        val resourceReferences =
            klasseResources.map { klasse ->
                mapToResourceReference(
                    klasse.klasseId,
                    ResourceReferenceDisplayNameBuilder()
                        .functionalId(klasse.klasseId)
                        .name(klasse.tittel),
                )
            }

        return if (resourceReferences.isNotEmpty()) {
            ResponseEntity.ok(resourceReferences)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("arkivdel")
    @Operation(summary = "List arkivdeler")
    fun getArkivdel(): ResponseEntity<Collection<ResourceReference>> =
        ResponseEntity.ok(
            arkivdelResourceCache
                .getAllDistinct()
                .map { arkivdelResource ->
                    mapToResourceReference(
                        arkivdelResource,
                        ResourceReferenceDisplayNameBuilder()
                            .technicalId(arkivdelResource.systemId)
                            .name(arkivdelResource.tittel),
                    )
                },
        )

    @GetMapping("arkivressurs")
    @Operation(summary = "List arkivressurser")
    fun getArkivressurs(): ResponseEntity<Collection<ResourceReference>> =
        ResponseEntity.ok(
            arkivressursResourceCache
                .getAllDistinct()
                .map { arkivressurs ->
                    val functionalId =
                        arkivressursDisplayNameMapper
                            .findPersonalressursBrukernavn(arkivressurs)
                            .orElse(null)
                    val name =
                        arkivressursDisplayNameMapper
                            .findPersonNavn(arkivressurs)
                            .orElse(null)
                    val technicalId = arkivressurs.systemId?.identifikatorverdi
                    val displayNameBuilder =
                        ResourceReferenceDisplayNameBuilder()
                            .technicalId(technicalId)
                            .functionalId(functionalId)
                            .name(name)

                    ResourceReference(
                        id = ResourceLinkUtil.getFirstSelfLink(arkivressurs),
                        displayName = displayNameBuilder.build(),
                        functionalId = functionalId.orEmpty(),
                        name = name.orEmpty(),
                        technicalId = technicalId.orEmpty(),
                    )
                },
        )

    @GetMapping("partrolle")
    @Operation(summary = "List partroller")
    fun getPartRolle(): ResponseEntity<Collection<ResourceReference>> =
        getBegrepResourceReferences(partRolleResourceCache)

    @GetMapping("korrespondanseparttype")
    @Operation(summary = "List korrespondanseparttyper")
    fun getKorrespondansepartType(): ResponseEntity<Collection<ResourceReference>> =
        getBegrepResourceReferences(korrespondansepartTypeResourceCache)

    @GetMapping("tilknyttetregistreringsom")
    @Operation(summary = "List tilknyttet registrering som")
    fun getTilknyttetRegistreringSom(): ResponseEntity<Collection<ResourceReference>> =
        getBegrepResourceReferences(tilknyttetRegistreringSomResourceCache)

    @GetMapping("sakstatus")
    @Operation(summary = "List saksstatuser")
    fun getSakstatus(): ResponseEntity<Collection<ResourceReference>> =
        getBegrepResourceReferences(saksstatusResourceCache)

    @GetMapping("skjermingshjemmel")
    @Operation(summary = "List skjermingshjemler")
    fun getSkjermingshjemmel(): ResponseEntity<Collection<ResourceReference>> =
        getBegrepResourceReferences(skjermingshjemmelResourceCache)

    @GetMapping("tilgangsrestriksjon")
    @Operation(summary = "List tilgangsrestriksjoner")
    fun getTilgangsrestriksjon(): ResponseEntity<Collection<ResourceReference>> =
        getBegrepResourceReferences(tilgangsrestriksjonResourceCache)

    @GetMapping("dokumentstatus")
    @Operation(summary = "List dokumentstatuser")
    fun getDokumentstatus(): ResponseEntity<Collection<ResourceReference>> =
        getBegrepResourceReferences(dokumentStatusResourceCache)

    @GetMapping("dokumenttype")
    @Operation(summary = "List dokumenttyper")
    fun getDokumenttype(): ResponseEntity<Collection<ResourceReference>> =
        getBegrepResourceReferences(dokumentTypeResourceCache)

    @GetMapping("journalstatus")
    @Operation(summary = "List journalstatuser")
    fun getJournalstatus(): ResponseEntity<Collection<ResourceReference>> =
        getBegrepResourceReferences(journalStatusResourceCache)

    @GetMapping("journalposttype")
    @Operation(summary = "List journalposttyper")
    fun getJournalposttype(): ResponseEntity<Collection<ResourceReference>> =
        getBegrepResourceReferences(journalpostTypeResourceCache)

    @GetMapping("saksmappetype")
    @Operation(summary = "List saksmappetyper")
    fun getSaksmappetype(): ResponseEntity<Collection<ResourceReference>> =
        getBegrepResourceReferences(saksmappetypeResourceCache)

    @GetMapping("variantformat")
    @Operation(summary = "List variantformater")
    fun getVariantformat(): ResponseEntity<Collection<ResourceReference>> =
        getBegrepResourceReferences(variantformatResourceCache)

    @GetMapping("format")
    @Operation(summary = "List formater")
    fun getFormat(): ResponseEntity<Collection<ResourceReference>> = getBegrepResourceReferences(formatResourceCache)

    @GetMapping("tilgangsgruppe")
    @Operation(summary = "List tilgangsgrupper")
    fun getTilgangsgruppe(): ResponseEntity<Collection<ResourceReference>> =
        getBegrepResourceReferences(tilgangsgruppeResourceCache)

    private fun <R> getBegrepResourceReferences(
        resourceCache: FintCache<String, R>,
    ): ResponseEntity<Collection<ResourceReference>>
        where R : Begrep,
              R : FintLinks =
        ResponseEntity.ok(resourceCache.getAllDistinct().map(this::mapToResourceReference))

    private fun <R> mapToResourceReference(
        resource: R,
    ): ResourceReference
        where R : Begrep,
              R : FintLinks =
        ResourceReference(
            ResourceLinkUtil.getFirstSelfLink(resource),
            ResourceReferenceDisplayNameBuilder()
                .functionalId(resource.kode)
                .technicalId(resource.systemId)
                .name(resource.navn)
                .build(),
        )

    private fun mapToResourceReference(
        resource: FintLinks,
        resourceReferenceDisplayNameBuilder: ResourceReferenceDisplayNameBuilder,
    ): ResourceReference =
        ResourceReference(ResourceLinkUtil.getFirstSelfLink(resource), resourceReferenceDisplayNameBuilder.build())

    private fun mapToResourceReference(
        id: String,
        resourceReferenceDisplayNameBuilder: ResourceReferenceDisplayNameBuilder,
    ): ResourceReference = ResourceReference(id, resourceReferenceDisplayNameBuilder.build())

    private class ResourceReferenceDisplayNameBuilder {
        private var functionalId: String? = null
        private var technicalId: String? = null
        private var name: String? = null

        fun functionalId(functionalId: String?) = apply { this.functionalId = functionalId }

        fun technicalId(technicalId: Identifikator?) = apply { this.technicalId = technicalId?.identifikatorverdi }

        fun technicalId(technicalId: String?) = apply { this.technicalId = technicalId }

        fun name(name: String?) = apply { this.name = name }

        fun build(): String =
            buildList {
                functionalId?.let { add("[$it]") }
                name?.let(::add)
                technicalId?.let { add("#$it") }
            }.joinToString(" ")
    }
}
