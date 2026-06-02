package no.novari.flyt.archive.gateway.resource.kodeverk

import no.novari.cache.FintCache
import no.novari.cache.exceptions.NoSuchCacheEntryException
import no.novari.fint.model.felles.kompleksedatatyper.Identifikator
import no.novari.fint.model.felles.kompleksedatatyper.Personnavn
import no.novari.fint.model.resource.Link
import no.novari.fint.model.resource.administrasjon.personal.PersonalressursResource
import no.novari.fint.model.resource.arkiv.noark.ArkivressursResource
import no.novari.fint.model.resource.felles.PersonResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class ArkivressursDisplayNameMapperTest {
    @Mock
    lateinit var personalressursResourceCache: FintCache<String, PersonalressursResource>

    @Mock
    lateinit var personResourceCache: FintCache<String, PersonResource>

    private lateinit var arkivressursDisplayNameMapper: ArkivressursDisplayNameMapper

    @BeforeEach
    fun setUp() {
        arkivressursDisplayNameMapper = ArkivressursDisplayNameMapper(personalressursResourceCache, personResourceCache)
    }

    @Test
    fun `findPersonNavn given first, middle and last name, returns the full name`() {
        val personnavn: Personnavn = mock()
        whenever(personnavn.fornavn).thenReturn("testFornavn")
        whenever(personnavn.mellomnavn).thenReturn("testMellomnavn")
        whenever(personnavn.etternavn).thenReturn("testEtternavn")

        val arkivressursResource = setupMocksForPersonnavn(personnavn)

        assertThat(arkivressursDisplayNameMapper.findPersonNavn(arkivressursResource))
            .contains("testFornavn testMellomnavn testEtternavn")
    }

    @Test
    fun `findPersonNavn given a null name, throws IllegalStateException`() {
        val arkivressursResource = setupMocksForPersonnavn(null)

        val error =
            assertThrows<IllegalStateException> {
                arkivressursDisplayNameMapper.findPersonNavn(arkivressursResource)
            }

        assertThat(error).hasMessage("Person resource contains no name")
    }

    @Test
    fun `findPersonNavn given no matching personalressurs in cache, returns empty`() {
        val arkivressursResource: ArkivressursResource = mock()
        whenever(arkivressursResource.personalressurs).thenReturn(listOf(Link.with("a/b/c/testPersonalressursLink1")))
        whenever(personalressursResourceCache.get("a/b/c/testPersonalressursLink1"))
            .thenThrow(NoSuchCacheEntryException("a/b/c/testPersonalressursLink1"))

        assertThat(arkivressursDisplayNameMapper.findPersonNavn(arkivressursResource)).isEmpty
    }

    @Test
    fun `findPersonNavn uses the first links to look up in the cache`() {
        val arkivressursResource: ArkivressursResource = mock()
        whenever(arkivressursResource.personalressurs)
            .thenReturn(
                listOf(
                    Link.with("a/b/c/testPersonalressursLink1"),
                    Link.with("a/b/c/testPersonalressursLink2"),
                ),
            )

        val personalressursResource: PersonalressursResource = mock()
        whenever(
            this.personalressursResourceCache.get("a/b/c/testPersonalressursLink1"),
        ).thenReturn(personalressursResource)
        whenever(
            personalressursResource.person,
        ).thenReturn(listOf(Link.with("a/b/c/testPersonLink1"), Link.with("a/b/c/testPersonLink2")))
        val personResource: PersonResource = mock()
        whenever(personResourceCache.get("a/b/c/testPersonLink1")).thenReturn(personResource)
        val personnavn: Personnavn = mock()
        whenever(personResource.navn).thenReturn(personnavn)

        arkivressursDisplayNameMapper.findPersonNavn(arkivressursResource)

        verify(personalressursResourceCache, times(1)).get("a/b/c/testPersonalressursLink1")
        verifyNoMoreInteractions(personalressursResourceCache)
        verify(personResourceCache, times(1)).get("a/b/c/testPersonLink1")
        verifyNoMoreInteractions(personResourceCache)
    }

    @Test
    fun `findPersonalressursBrukernavn given a matching personalressurs in the cache, returns the identifikator`() {
        val arkivressursResource = setupMocksForPersonBrukernavn()

        assertThat(arkivressursDisplayNameMapper.findPersonalressursBrukernavn(arkivressursResource)).contains("12345")
    }

    private fun setupMocksForPersonnavn(personnavn: Personnavn?): ArkivressursResource {
        val arkivressursResource: ArkivressursResource = mock()
        whenever(arkivressursResource.personalressurs).thenReturn(listOf(Link.with("a/b/c/testPersonalressursLink1")))

        val personalressursResource: PersonalressursResource = mock()
        whenever(personalressursResourceCache.get("a/b/c/testPersonalressursLink1")).thenReturn(personalressursResource)
        whenever(personalressursResource.person).thenReturn(listOf(Link.with("a/b/c/testPersonLink1")))

        val personResource: PersonResource = mock()
        whenever(personResourceCache.get("a/b/c/testPersonLink1")).thenReturn(personResource)
        whenever(personResource.navn).thenReturn(personnavn)

        return arkivressursResource
    }

    private fun setupMocksForPersonBrukernavn(): ArkivressursResource {
        val arkivressursResource: ArkivressursResource = mock()
        whenever(arkivressursResource.personalressurs).thenReturn(listOf(Link.with("a/b/c/testPersonalressursLink1")))

        val personalressursResource: PersonalressursResource = mock()
        whenever(personalressursResourceCache.getOptional("a/b/c/testPersonalressursLink1"))
            .thenReturn(Optional.of(personalressursResource))

        val identifikator = Identifikator()
        identifikator.identifikatorverdi = "12345"
        whenever(personalressursResource.brukernavn).thenReturn(identifikator)

        return arkivressursResource
    }
}
