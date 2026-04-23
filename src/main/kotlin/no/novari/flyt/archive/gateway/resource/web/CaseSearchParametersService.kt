package no.novari.flyt.archive.gateway.resource.web

import no.novari.cache.FintCache
import no.novari.fint.model.resource.arkiv.kodeverk.SaksmappetypeResource
import no.novari.fint.model.resource.arkiv.kodeverk.SaksstatusResource
import no.novari.fint.model.resource.arkiv.kodeverk.TilgangsrestriksjonResource
import no.novari.fint.model.resource.arkiv.noark.AdministrativEnhetResource
import no.novari.fint.model.resource.arkiv.noark.ArkivdelResource
import no.novari.fint.model.resource.arkiv.noark.KlassifikasjonssystemResource
import no.novari.flyt.archive.gateway.dispatch.model.instance.CaseSearchParametersDto
import no.novari.flyt.archive.gateway.dispatch.model.instance.KlasseDto
import no.novari.flyt.archive.gateway.dispatch.model.instance.SakDto
import no.novari.flyt.archive.gateway.resource.web.exceptions.KlasseOrderOutOfBoundsException
import no.novari.flyt.archive.gateway.resource.web.exceptions.SearchKlasseOrderNotFoundInCaseException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class CaseSearchParametersService(
    private val arkivdelResourceCache: FintCache<String, ArkivdelResource>,
    private val administrativEnhetResourceCache: FintCache<String, AdministrativEnhetResource>,
    private val tilgangsrestriksjonResourceCache: FintCache<String, TilgangsrestriksjonResource>,
    private val saksmappetypeResourceCache: FintCache<String, SaksmappetypeResource>,
    private val saksstatusResourceCache: FintCache<String, SaksstatusResource>,
    private val klassifikasjonssystemResourceCache: FintCache<String, KlassifikasjonssystemResource>,
) {
    fun createFilterQueryParamValue(
        sakDto: SakDto,
        caseSearchParametersDto: CaseSearchParametersDto,
    ): String {
        val filterParts = mutableListOf<String>()

        if (caseSearchParametersDto.arkivdel) {
            sakDto.arkivdel
                ?.let(arkivdelResourceCache::get)
                ?.systemId
                ?.identifikatorverdi
                ?.let { createFilterLine("arkivdel", it) }
                ?.let(filterParts::add)
        }
        if (caseSearchParametersDto.administrativEnhet) {
            sakDto.administrativEnhet
                ?.let(administrativEnhetResourceCache::get)
                ?.systemId
                ?.identifikatorverdi
                ?.let { createFilterLine("administrativenhet", it) }
                ?.let(filterParts::add)
        }
        if (caseSearchParametersDto.tilgangsrestriksjon) {
            sakDto.skjerming
                ?.tilgangsrestriksjon
                ?.let(tilgangsrestriksjonResourceCache::get)
                ?.systemId
                ?.identifikatorverdi
                ?.let { createFilterLine("tilgangskode", it) }
                ?.let(filterParts::add)
        }
        if (caseSearchParametersDto.saksmappetype) {
            sakDto.saksmappetype
                ?.let(saksmappetypeResourceCache::get)
                ?.systemId
                ?.identifikatorverdi
                ?.let { createFilterLine("saksmappetype", it) }
                ?.let(filterParts::add)
        }
        if (caseSearchParametersDto.saksstatus) {
            sakDto.saksstatus
                ?.let(saksstatusResourceCache::get)
                ?.systemId
                ?.identifikatorverdi
                ?.let { createFilterLine("saksstatus", it) }
                ?.let(filterParts::add)
        }
        if (caseSearchParametersDto.tittel) {
            sakDto.tittel
                ?.let { createFilterLine("tittel", it) }
                ?.let(filterParts::add)
        }
        if (caseSearchParametersDto.klassering) {
            caseSearchParametersDto.klasseringRekkefolge
                ?.toIntOrNull()
                ?.let { rekkefolge ->
                    val klasseDtoMatchingRekkefolge =
                        sakDto.klasse
                            ?.firstOrNull { klasseDto -> klasseDto.rekkefolge == rekkefolge }
                            ?: throw SearchKlasseOrderNotFoundInCaseException(
                                sakDto.klasse.orEmpty().mapNotNull(KlasseDto::rekkefolge),
                                rekkefolge,
                            )

                    klasseDtoMatchingRekkefolge.rekkefolge?.let { log.debug("KlasseDto rekkefolge: {}", it) }
                    log.debug("Søkeparametere rekkefølge: {}", rekkefolge)

                    if (caseSearchParametersDto.getKlasseringKlassifikasjonssystem()) {
                        klasseDtoMatchingRekkefolge.klassifikasjonssystem
                            ?.let(klassifikasjonssystemResourceCache::get)
                            ?.systemId
                            ?.identifikatorverdi
                            ?.let { value ->
                                createFilterLine(
                                    "${createKlasseringPrefix(rekkefolge)}ordning",
                                    value,
                                )
                            }?.let(filterParts::add)
                    }
                    if (caseSearchParametersDto.getKlasseringKlasseId()) {
                        klasseDtoMatchingRekkefolge.klasseId
                            ?.let { klasseId ->
                                createFilterLine(
                                    "${createKlasseringPrefix(rekkefolge)}verdi",
                                    klasseId,
                                )
                            }?.let(filterParts::add)
                    }
                }
        }

        return filterParts.joinToString(" and ")
    }

    private fun createFilterLine(
        name: String,
        value: String,
    ): String = "$name eq '$value'"

    private fun createKlasseringPrefix(rekkefolge: Int): String {
        val klassifikasjonName =
            when(rekkefolge) {
                1 -> "primar"
                2 -> "sekundar"
                3 -> "tertiar"
                else -> throw KlasseOrderOutOfBoundsException(rekkefolge)
            }

        return "klassifikasjon/$klassifikasjonName/"
    }

    companion object {
        private val log = LoggerFactory.getLogger(CaseSearchParametersService::class.java)
    }
}
