package no.novari.flyt.archive.gateway.sak

import no.novari.fint.model.resource.arkiv.noark.JournalpostResource
import no.novari.fint.model.resource.arkiv.noark.SakResource
import no.novari.flyt.archive.gateway.resource.sak.CaseService
import no.novari.flyt.archive.gateway.resource.sak.InvalidArchiveInstanceIdException
import no.novari.flyt.archive.gateway.resource.web.FintArchiveResourceClient
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException

@ExtendWith(MockitoExtension::class)
class CaseServiceTest {
    @Mock
    lateinit var fintArchiveResourceClient: FintArchiveResourceClient

    private lateinit var caseService: CaseService

    @BeforeEach
    fun setUp() {
        caseService = CaseService(fintArchiveResourceClient)
    }

    @Test
    fun `getByMappeId returns case from FINT`() {
        val sakResource = SakResource().apply { tittel = "Tittel" }
        whenever(fintArchiveResourceClient.getResource("/arkiv/noark/sak/mappeid/2023/102", SakResource::class.java))
            .thenReturn(sakResource)

        assertThat(caseService.getByMappeId("2023/102")).isSameAs(sakResource)
    }

    @Test
    fun `getByMappeId returns null when FINT call fails`() {
        whenever(fintArchiveResourceClient.getResource("/arkiv/noark/sak/mappeid/2023/101", SakResource::class.java))
            .thenThrow(HttpClientErrorException(HttpStatus.NOT_FOUND))

        assertThat(caseService.getByMappeId("2023/101")).isNull()
    }

    @Test
    fun `getByArchiveInstanceIdWithFilteredJournalposts keeps only requested journalposts`() {
        val sakResource =
            SakResource().apply {
                journalpost = listOf(1L, 2L, 3L).map { JournalpostResource().apply { journalPostnummer = it } }
            }
        whenever(fintArchiveResourceClient.getResource("/arkiv/noark/sak/mappeid/2023/102", SakResource::class.java))
            .thenReturn(sakResource)

        val result = caseService.getByArchiveInstanceIdWithFilteredJournalposts("2023/102-[1,3]")

        assertThat(result!!.journalpost.map(JournalpostResource::getJournalPostnummer)).containsExactly(1L, 3L)
    }

    @Test
    fun `getByArchiveInstanceIdWithFilteredJournalposts returns null when FINT call fails`() {
        whenever(fintArchiveResourceClient.getResource("/arkiv/noark/sak/mappeid/2023/101", SakResource::class.java))
            .thenThrow(HttpClientErrorException(HttpStatus.NOT_FOUND))

        assertThat(caseService.getByArchiveInstanceIdWithFilteredJournalposts("2023/101-[1]")).isNull()
    }

    @Test
    fun `findByMappeId returns null when FINT has no sak`() {
        whenever(fintArchiveResourceClient.getResource("/arkiv/noark/sak/mappeid/2023/101", SakResource::class.java))
            .thenThrow(HttpClientErrorException(HttpStatus.NOT_FOUND))

        assertThat(caseService.findByMappeId("2023/101")).isNull()
    }

    @Test
    fun `findByMappeId throws when FINT call fails`() {
        whenever(fintArchiveResourceClient.getResource("/arkiv/noark/sak/mappeid/2023/101", SakResource::class.java))
            .thenThrow(HttpServerErrorException(HttpStatus.BAD_GATEWAY))

        assertThatThrownBy { caseService.findByMappeId("2023/101") }
            .isInstanceOf(HttpServerErrorException::class.java)
    }

    @Test
    fun `findByArchiveInstanceIdWithFilteredJournalposts returns null when FINT has no sak`() {
        whenever(fintArchiveResourceClient.getResource("/arkiv/noark/sak/mappeid/2023/101", SakResource::class.java))
            .thenThrow(HttpClientErrorException(HttpStatus.NOT_FOUND))

        assertThat(caseService.findByArchiveInstanceIdWithFilteredJournalposts("2023/101-[1]")).isNull()
    }

    @Test
    fun `findByArchiveInstanceIdWithFilteredJournalposts throws when FINT call fails`() {
        whenever(fintArchiveResourceClient.getResource("/arkiv/noark/sak/mappeid/2023/101", SakResource::class.java))
            .thenThrow(HttpServerErrorException(HttpStatus.BAD_GATEWAY))

        assertThatThrownBy { caseService.findByArchiveInstanceIdWithFilteredJournalposts("2023/101-[1]") }
            .isInstanceOf(HttpServerErrorException::class.java)
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "2023/101-[x]",
            "2023/101-[1]-[2]",
            "2023/101-[-1]",
            "2023/101-[]",
            "2023/101-[1,]",
            "2023/101-1",
            "2023/101-",
            "",
        ],
    )
    fun `findByArchiveInstanceIdWithFilteredJournalposts throws when archiveInstanceId is invalid`(
        archiveInstanceId: String,
    ) {
        assertThatThrownBy { caseService.findByArchiveInstanceIdWithFilteredJournalposts(archiveInstanceId) }
            .isInstanceOf(InvalidArchiveInstanceIdException::class.java)
    }

    @Test
    fun `findByArchiveInstanceIdWithFilteredJournalposts accepts sak id without journalposts`() {
        val sakResource =
            SakResource().apply {
                journalpost = listOf(1L).map { JournalpostResource().apply { journalPostnummer = it } }
            }
        whenever(fintArchiveResourceClient.getResource("/arkiv/noark/sak/mappeid/2023/102", SakResource::class.java))
            .thenReturn(sakResource)

        val result = caseService.findByArchiveInstanceIdWithFilteredJournalposts("2023/102")

        assertThat(result!!.journalpost).isEmpty()
    }
}
