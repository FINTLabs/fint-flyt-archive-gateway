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
        val personalressursResourceHref = getPersonalressursResourceHref(arkivressursResource)
        val personalressursResource = personalressursResourceCache.get(personalressursResourceHref)

        val personResourceHref = getPersonResourceHref(personalressursResource)
        val personResource = personResourceCache.get(personResourceHref)

        val personnavn = personResource.navn ?: throw IllegalStateException("Person resource contains no name")
        return listOfNotNull(personnavn.fornavn, personnavn.mellomnavn, personnavn.etternavn)
            .joinToString(" ")
    }

    private fun getPersonalressursBrukernavn(arkivressursResource: ArkivressursResource): String {
        val personalressursResourceHref = getPersonalressursResourceHref(arkivressursResource)

        return personalressursResourceCache
            .getOptional(personalressursResourceHref)
            .map(PersonalressursResource::getBrukernavn)
            .map(Identifikator::getIdentifikatorverdi)
            .orElseGet { personalressursResourceHref.substringAfterLast('/') }
    }

    private fun getPersonalressursResourceHref(arkivressursResource: ArkivressursResource): String =
        ResourceLinkUtil.getFirstLink(arkivressursResource::getPersonalressurs, arkivressursResource, "Personalressurs")

    private fun getPersonResourceHref(personalressursResource: PersonalressursResource): String =
        ResourceLinkUtil.getFirstLink(personalressursResource::getPerson, personalressursResource, "Person")
}
