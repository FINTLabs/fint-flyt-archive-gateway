package no.novari.flyt.archive.gateway.sak

import no.novari.fint.model.resource.arkiv.noark.SakResource
import no.novari.flyt.archive.gateway.resource.sak.CaseService
import no.novari.flyt.archive.gateway.resource.sak.InternalClientCaseController
import no.novari.flyt.archive.gateway.resource.sak.InvalidArchiveInstanceIdException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@ExtendWith(MockitoExtension::class)
class InternalClientCaseControllerTest {
    @Mock
    lateinit var caseService: CaseService

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(InternalClientCaseController(caseService)).build()
    }

    @Test
    fun `getCase returns sak`() {
        whenever(caseService.findByMappeId("2023/102")).thenReturn(SakResource().apply { tittel = "Tittel" })

        mockMvc
            .perform(get("$PATH/2023/102"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.tittel").value("Tittel"))
    }

    @Test
    fun `getCase returns 404 when sak is not found`() {
        whenever(caseService.findByMappeId("2023/101")).thenReturn(null)

        mockMvc.perform(get("$PATH/2023/101")).andExpect(status().isNotFound)
    }

    @Test
    fun `getCaseWithFilteredJournalposts returns sak`() {
        whenever(caseService.findByArchiveInstanceIdWithFilteredJournalposts("2023/102-[1,3]"))
            .thenReturn(SakResource().apply { tittel = "Tittel" })

        mockMvc
            .perform(get(PATH).queryParam("archiveInstanceId", "2023/102-[1,3]"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.tittel").value("Tittel"))
    }

    @Test
    fun `getCaseWithFilteredJournalposts returns 404 when sak is not found`() {
        whenever(caseService.findByArchiveInstanceIdWithFilteredJournalposts("2023/101-[1]")).thenReturn(null)

        mockMvc
            .perform(get(PATH).queryParam("archiveInstanceId", "2023/101-[1]"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `getCaseWithFilteredJournalposts returns 400 when archiveInstanceId is invalid`() {
        whenever(caseService.findByArchiveInstanceIdWithFilteredJournalposts("2023/101-[x]"))
            .thenThrow(InvalidArchiveInstanceIdException("2023/101-[x]"))

        mockMvc
            .perform(get(PATH).queryParam("archiveInstanceId", "2023/101-[x]"))
            .andExpect(status().isBadRequest)
    }

    companion object {
        private const val PATH = "/api/intern-klient/arkiv/saker"
    }
}
