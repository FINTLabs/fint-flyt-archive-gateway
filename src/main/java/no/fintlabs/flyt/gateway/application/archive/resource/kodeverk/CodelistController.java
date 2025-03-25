package no.fintlabs.flyt.gateway.application.archive.resource.kodeverk;

import no.fint.model.felles.basisklasser.Begrep;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.FintLinks;
import no.fint.model.resource.arkiv.kodeverk.*;
import no.fint.model.resource.arkiv.noark.AdministrativEnhetResource;
import no.fint.model.resource.arkiv.noark.ArkivdelResource;
import no.fint.model.resource.arkiv.noark.ArkivressursResource;
import no.fint.model.resource.arkiv.noark.KlassifikasjonssystemResource;
import no.fintlabs.cache.FintCache;
import no.fintlabs.flyt.gateway.application.archive.links.ResourceLinkUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static no.fintlabs.resourceserver.UrlPaths.INTERNAL_API;

@RestController
@RequestMapping(INTERNAL_API + "/arkiv/kodeverk")
public class CodelistController {

    private final FintCache<String, AdministrativEnhetResource> administrativEnhetResourceCache;
    private final FintCache<String, ArkivdelResource> arkivdelResourceCache;
    private final FintCache<String, ArkivressursResource> arkivressursResourceCache;
    private final FintCache<String, TilknyttetRegistreringSomResource> tilknyttetRegistreringSomResourceCache;
    private final FintCache<String, DokumentStatusResource> dokumentStatusResourceCache;
    private final FintCache<String, DokumentTypeResource> dokumentTypeResourceCache;
    private final FintCache<String, KlassifikasjonssystemResource> klassifikasjonssystemResourceCache;
    private final FintCache<String, PartRolleResource> partRolleResourceCache;
    private final FintCache<String, KorrespondansepartTypeResource> korrespondansepartTypeResourceCache;
    private final FintCache<String, SaksstatusResource> saksstatusResourceCache;
    private final FintCache<String, SkjermingshjemmelResource> skjermingshjemmelResourceCache;
    private final FintCache<String, TilgangsrestriksjonResource> tilgangsrestriksjonResourceCache;
    private final FintCache<String, JournalStatusResource> journalStatusResourceCache;
    private final FintCache<String, JournalpostTypeResource> journalpostTypeResourceCache;
    private final FintCache<String, SaksmappetypeResource> saksmappetypeResourceCache;
    private final FintCache<String, VariantformatResource> variantformatResourceCache;
    private final FintCache<String, FormatResource> formatResourceCache;
    private final FintCache<String, TilgangsgruppeResource> tilgangsgruppeResourceCache;
    private final ArkivressursDisplayNameMapper arkivressursDisplayNameMapper;

    public CodelistController(
            FintCache<String, AdministrativEnhetResource> administrativEnhetResourceCache,
            FintCache<String, ArkivdelResource> arkivdelResourceCache,
            FintCache<String, ArkivressursResource> arkivressursResourceCache,
            FintCache<String, TilknyttetRegistreringSomResource> tilknyttetRegistreringSomResourceCache,
            FintCache<String, DokumentStatusResource> dokumentStatusResourceCache,
            FintCache<String, DokumentTypeResource> dokumentTypeResourceCache,
            FintCache<String, KlassifikasjonssystemResource> klassifikasjonssystemResourceCache,
            FintCache<String, PartRolleResource> partRolleResourceCache,
            FintCache<String, KorrespondansepartTypeResource> korrespondansepartTypeResourceCache,
            FintCache<String, SaksstatusResource> saksstatusResourceCache,
            FintCache<String, SkjermingshjemmelResource> skjermingshjemmelResourceCache,
            FintCache<String, TilgangsrestriksjonResource> tilgangsrestriksjonResourceCache,
            FintCache<String, JournalStatusResource> journalStatusResourceCache,
            FintCache<String, JournalpostTypeResource> journalpostTypeResourceCache,
            FintCache<String, SaksmappetypeResource> saksmappetypeResourceCache,
            FintCache<String, VariantformatResource> variantformatResourceCache,
            FintCache<String, FormatResource> formatResourceCache,
            FintCache<String, TilgangsgruppeResource> tilgangsgruppeResourceCache,
            ArkivressursDisplayNameMapper arkivressursDisplayNameMapper
    ) {
        this.administrativEnhetResourceCache = administrativEnhetResourceCache;
        this.arkivdelResourceCache = arkivdelResourceCache;
        this.arkivressursResourceCache = arkivressursResourceCache;
        this.tilknyttetRegistreringSomResourceCache = tilknyttetRegistreringSomResourceCache;
        this.dokumentStatusResourceCache = dokumentStatusResourceCache;
        this.dokumentTypeResourceCache = dokumentTypeResourceCache;
        this.klassifikasjonssystemResourceCache = klassifikasjonssystemResourceCache;
        this.partRolleResourceCache = partRolleResourceCache;
        this.korrespondansepartTypeResourceCache = korrespondansepartTypeResourceCache;
        this.saksstatusResourceCache = saksstatusResourceCache;
        this.skjermingshjemmelResourceCache = skjermingshjemmelResourceCache;
        this.tilgangsrestriksjonResourceCache = tilgangsrestriksjonResourceCache;
        this.journalStatusResourceCache = journalStatusResourceCache;
        this.journalpostTypeResourceCache = journalpostTypeResourceCache;
        this.saksmappetypeResourceCache = saksmappetypeResourceCache;
        this.variantformatResourceCache = variantformatResourceCache;
        this.formatResourceCache = formatResourceCache;
        this.tilgangsgruppeResourceCache = tilgangsgruppeResourceCache;
        this.arkivressursDisplayNameMapper = arkivressursDisplayNameMapper;
    }

    @GetMapping("administrativenhet")
    public ResponseEntity<Collection<ResourceReference>> getAdministrativEnheter() {
        return ResponseEntity.ok(
                administrativEnhetResourceCache
                        .getAllDistinct()
                        .stream()
                        .map(administrativEnhetResource -> this.mapToResourceReference(
                                administrativEnhetResource,
                                new ResourceReferenceDisplayNameBuilder()
                                        .technicalId(administrativEnhetResource.getSystemId())
                                        .name(administrativEnhetResource.getNavn())
                        ))
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("klassifikasjonssystem")
    public ResponseEntity<Collection<ResourceReference>> getKlassifikasjonssystem() {
        return ResponseEntity.ok(
                klassifikasjonssystemResourceCache
                        .getAllDistinct()
                        .stream()
                        .map(klassifikasjonssystemResource -> this.mapToResourceReference(
                                klassifikasjonssystemResource,
                                new ResourceReferenceDisplayNameBuilder()
                                        .technicalId(klassifikasjonssystemResource.getSystemId())
                                        .name(klassifikasjonssystemResource.getTittel())
                        ))
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("klasse")
    public ResponseEntity<Collection<ResourceReference>> getKlasse(@RequestParam String klassifikasjonssystemLink) {
        return klassifikasjonssystemResourceCache.getOptional(klassifikasjonssystemLink)
                .map(KlassifikasjonssystemResource::getKlasse)
                .<Collection<ResourceReference>>map(klasseResources -> klasseResources.stream()
                        .map(klasse -> this.mapToResourceReference(
                                klasse.getKlasseId(),
                                new ResourceReferenceDisplayNameBuilder()
                                        .functionalId(klasse.getKlasseId())
                                        .name(klasse.getTittel())
                        )).toList()
                )
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("arkivdel")
    public ResponseEntity<Collection<ResourceReference>> getArkivdel() {
        return ResponseEntity.ok(
                arkivdelResourceCache
                        .getAllDistinct()
                        .stream()
                        .map(arkivdelResource -> this.mapToResourceReference(
                                arkivdelResource,
                                new ResourceReferenceDisplayNameBuilder()
                                        .technicalId(arkivdelResource.getSystemId())
                                        .name(arkivdelResource.getTittel())
                        ))
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("arkivressurs")
    public ResponseEntity<Collection<ResourceReference>> getArkivressurs() {
        return ResponseEntity.ok(
                arkivressursResourceCache
                        .getAllDistinct()
                        .stream()
                        .map(arkivressurs -> {
                            ResourceReferenceDisplayNameBuilder resourceReferenceDisplayNameBuilder = new ResourceReferenceDisplayNameBuilder()
                                    .technicalId(arkivressurs.getSystemId());

                            arkivressursDisplayNameMapper.findPersonalressursBrukernavn(arkivressurs)
                                    .ifPresent(resourceReferenceDisplayNameBuilder::functionalId);

                            arkivressursDisplayNameMapper.findPersonNavn(arkivressurs)
                                    .ifPresent(resourceReferenceDisplayNameBuilder::name);

                            return mapToResourceReference(arkivressurs, resourceReferenceDisplayNameBuilder);
                        })
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("partrolle")
    public ResponseEntity<Collection<ResourceReference>> getPartRolle() {
        return getBegrepResourceReferences(partRolleResourceCache);
    }

    @GetMapping("korrespondanseparttype")
    public ResponseEntity<Collection<ResourceReference>> getKorrespondansepartType() {
        return getBegrepResourceReferences(korrespondansepartTypeResourceCache);
    }

    @GetMapping("tilknyttetregistreringsom")
    public ResponseEntity<Collection<ResourceReference>> getTilknyttetRegistreringSom() {
        return getBegrepResourceReferences(tilknyttetRegistreringSomResourceCache);
    }

    @GetMapping("sakstatus")
    public ResponseEntity<Collection<ResourceReference>> getSakstatus() {
        return getBegrepResourceReferences(saksstatusResourceCache);
    }

    @GetMapping("skjermingshjemmel")
    public ResponseEntity<Collection<ResourceReference>> getSkjermingshjemmel() {
        return getBegrepResourceReferences(skjermingshjemmelResourceCache);
    }

    @GetMapping("tilgangsrestriksjon")
    public ResponseEntity<Collection<ResourceReference>> getTilgangsrestriksjon() {
        return getBegrepResourceReferences(tilgangsrestriksjonResourceCache);
    }

    @GetMapping("dokumentstatus")
    public ResponseEntity<Collection<ResourceReference>> getDokumentstatus() {
        return getBegrepResourceReferences(dokumentStatusResourceCache);
    }

    @GetMapping("dokumenttype")
    public ResponseEntity<Collection<ResourceReference>> getDokumenttype() {
        return getBegrepResourceReferences(dokumentTypeResourceCache);
    }

    @GetMapping("journalstatus")
    public ResponseEntity<Collection<ResourceReference>> getJournalstatus() {
        return getBegrepResourceReferences(journalStatusResourceCache);
    }

    @GetMapping("journalposttype")
    public ResponseEntity<Collection<ResourceReference>> getJournalposttype() {
        return getBegrepResourceReferences(journalpostTypeResourceCache);
    }

    @GetMapping("saksmappetype")
    public ResponseEntity<Collection<ResourceReference>> getSaksmappetype() {
        return getBegrepResourceReferences(saksmappetypeResourceCache);
    }

    @GetMapping("variantformat")
    public ResponseEntity<Collection<ResourceReference>> getVariantformat() {
        return getBegrepResourceReferences(variantformatResourceCache);
    }

    @GetMapping("format")
    public ResponseEntity<Collection<ResourceReference>> getFormat() {
        return getBegrepResourceReferences(formatResourceCache);
    }

    @GetMapping("tilgangsgruppe")
    public ResponseEntity<Collection<ResourceReference>> getTilgangsgruppe() {
        return getBegrepResourceReferences(tilgangsgruppeResourceCache);
    }

    private <R extends Begrep & FintLinks> ResponseEntity<Collection<ResourceReference>> getBegrepResourceReferences(FintCache<String, R> resouceCache) {
        return ResponseEntity.ok(
                resouceCache
                        .getAllDistinct()
                        .stream()
                        .map(this::mapToResourceReference)
                        .collect(Collectors.toList())
        );
    }

    private <R extends Begrep & FintLinks> ResourceReference mapToResourceReference(R resource) {
        return new ResourceReference(
                ResourceLinkUtil.getFirstSelfLink(resource),
                new ResourceReferenceDisplayNameBuilder()
                        .functionalId(resource.getKode())
                        .technicalId(resource.getSystemId())
                        .name(resource.getNavn())
                        .build()
        );
    }

    private ResourceReference mapToResourceReference(FintLinks resource, ResourceReferenceDisplayNameBuilder resourceReferenceDisplayNameBuilder) {
        return new ResourceReference(
                ResourceLinkUtil.getFirstSelfLink(resource),
                resourceReferenceDisplayNameBuilder.build()
        );
    }

    private ResourceReference mapToResourceReference(String id, ResourceReferenceDisplayNameBuilder resourceReferenceDisplayNameBuilder) {
        return new ResourceReference(
                id,
                resourceReferenceDisplayNameBuilder.build()
        );
    }

    private static class ResourceReferenceDisplayNameBuilder {
        private String functionalId;
        private String technicalId;
        private String name;

        ResourceReferenceDisplayNameBuilder functionalId(String functionalId) {
            this.functionalId = functionalId;
            return this;
        }

        ResourceReferenceDisplayNameBuilder technicalId(Identifikator technicalId) {
            return technicalId(technicalId.getIdentifikatorverdi());
        }

        ResourceReferenceDisplayNameBuilder technicalId(String technicalId) {
            this.technicalId = technicalId;
            return this;
        }

        ResourceReferenceDisplayNameBuilder name(String name) {
            this.name = name;
            return this;
        }

        String build() {
            StringJoiner stringJoiner = new StringJoiner(" ");
            if (functionalId != null) {
                stringJoiner.add("[" + functionalId + "]");
            }
            if (name != null) {
                stringJoiner.add(name);
            }
            if (technicalId != null) {
                stringJoiner.add("#" + technicalId);
            }
            return stringJoiner.toString();
        }

    }

}
