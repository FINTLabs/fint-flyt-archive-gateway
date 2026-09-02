package no.novari.flyt.archive.gateway.resource.kodeverk

import no.novari.cache.FintCache
import no.novari.cache.exceptions.NoSuchCacheEntryException
import no.novari.cache.exceptions.NoSuchCacheException
import no.novari.fint.model.felles.kompleksedatatyper.Identifikator
import no.novari.fint.model.resource.administrasjon.personal.PersonalressursResource
import no.novari.fint.model.resource.arkiv.noark.ArkivressursResource
import no.novari.fint.model.resource.felles.PersonResource
import no.novari.flyt.archive.gateway.links.NoSuchLinkException
import no.novari.flyt.archive.gateway.links.ResourceLinkUtil
import org.springframework.stereotype.Service
import java.util.Optional

@Service
class ArkivressursDisplayNameMapper(
    private val personalressursResourceCache: FintCache<String, PersonalressursResource>,
    private val personResourceCache: FintCache<String, PersonResource>,
) {
    fun findPersonNavn(arkivressursResource: ArkivressursResource): Optional<String> =
        Optional.ofNullable(findPersonNavnOrNull(arkivressursResource))

    fun findPersonNavnOrNull(arkivressursResource: ArkivressursResource): String? {
        return try {
            getPersonNavn(arkivressursResource)
        } catch (_: NoSuchLinkException) {
            null
        } catch (_: NoSuchCacheException) {
            null
        } catch (_: NoSuchCacheEntryException) {
            null
        }
    }

    fun findPersonalressursBrukernavn(arkivressursResource: ArkivressursResource): Optional<String> =
        Optional.ofNullable(findPersonalressursBrukernavnOrNull(arkivressursResource))

    fun findPersonalressursBrukernavnOrNull(arkivressursResource: ArkivressursResource): String? {
        return try {
            getPersonalressursBrukernavn(arkivressursResource)
        } catch (_: NoSuchLinkException) {
            null
        } catch (_: NoSuchCacheException) {
            null
        } catch (_: NoSuchCacheEntryException) {
            null
        }
    }

    private fun getPersonNavn(arkivressursResource: ArkivressursResource): String {
        val personalressursResource = getPersonalressursResource(arkivressursResource)

        val personResourceHref = getPersonResourceHref(personalressursResource)
        val personResource = personResourceCache.get(personResourceHref)

        val personnavn = personResource.navn ?: throw IllegalStateException("Person resource contains no name")
        return listOfNotNull(personnavn.fornavn, personnavn.mellomnavn, personnavn.etternavn)
            .joinToString(" ")
    }

    private fun getPersonalressursBrukernavn(arkivressursResource: ArkivressursResource): String {
        val personalressursResourceHref = getPersonalressursResourceHrefOrNull(arkivressursResource)

        if (personalressursResourceHref != null) {
            return personalressursResourceCache
                .getOptional(personalressursResourceHref)
                .map(PersonalressursResource::getBrukernavn)
                .map(Identifikator::getIdentifikatorverdi)
                .orElseGet {
                    findPersonalressursResourceByAnsattnummerFromKildesystemId(arkivressursResource)
                        ?.let(::getPersonalressursBrukernavnOrNull)
                        ?: personalressursResourceHref.substringAfterLast('/')
                }
        }

        return findPersonalressursResourceByAnsattnummerFromKildesystemId(arkivressursResource)
            ?.let(::getPersonalressursBrukernavnOrNull)
            ?: throw NoSuchLinkException.noLink(arkivressursResource, "Personalressurs")
    }

    private fun getPersonalressursResource(arkivressursResource: ArkivressursResource): PersonalressursResource {
        val personalressursResourceHref = getPersonalressursResourceHrefOrNull(arkivressursResource)

        if (personalressursResourceHref != null) {
            return getPersonalressursResourceFromCacheOrNull(personalressursResourceHref)
                ?: findPersonalressursResourceByAnsattnummerFromKildesystemId(arkivressursResource)
                ?: throw NoSuchCacheEntryException(personalressursResourceHref)
        }

        return findPersonalressursResourceByAnsattnummerFromKildesystemId(arkivressursResource)
            ?: throw NoSuchLinkException.noLink(arkivressursResource, "Personalressurs")
    }

    private fun getPersonalressursResourceFromCacheOrNull(
        personalressursResourceHref: String,
    ): PersonalressursResource? =
        try {
            personalressursResourceCache.get(personalressursResourceHref)
        } catch (_: NoSuchCacheException) {
            null
        } catch (_: NoSuchCacheEntryException) {
            null
        }

    private fun findPersonalressursResourceByAnsattnummerFromKildesystemId(
        arkivressursResource: ArkivressursResource,
    ): PersonalressursResource? {
        val ansattnummer = findAnsattnummerInKildesystemId(arkivressursResource) ?: return null

        return personalressursResourceCache
            .getAllDistinct()
            .firstOrNull { personalressursResource ->
                personalressursResource.ansattnummer?.identifikatorverdi == ansattnummer
            }
    }

    private fun getPersonalressursResourceHref(arkivressursResource: ArkivressursResource): String =
        ResourceLinkUtil.getFirstLink(arkivressursResource::getPersonalressurs, arkivressursResource, "Personalressurs")

    private fun getPersonalressursResourceHrefOrNull(arkivressursResource: ArkivressursResource): String? =
        try {
            getPersonalressursResourceHref(arkivressursResource)
        } catch (_: NoSuchLinkException) {
            null
        }

    private fun getPersonResourceHref(personalressursResource: PersonalressursResource): String =
        ResourceLinkUtil.getFirstLink(personalressursResource::getPerson, personalressursResource, "Person")

    private fun getPersonalressursBrukernavnOrNull(personalressursResource: PersonalressursResource): String? =
        personalressursResource.brukernavn?.identifikatorverdi
            ?: personalressursResource.ansattnummer?.identifikatorverdi

    private fun findAnsattnummerInKildesystemId(arkivressursResource: ArkivressursResource): String? =
        arkivressursResource.kildesystemId
            ?.identifikatorverdi
            ?.takeIf { it.contains('_') }
            ?.substringBefore('_')
            ?.takeIf(String::isNotBlank)
}
