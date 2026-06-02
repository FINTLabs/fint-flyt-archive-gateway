package no.novari.flyt.archive.gateway.sak

import no.novari.fint.model.resource.arkiv.noark.SakResource
import no.novari.flyt.archive.gateway.resource.sak.CaseController
import no.novari.flyt.archive.gateway.resource.sak.CaseRequestService
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

@ExtendWith(MockitoExtension::class)
class CaseControllerTest {
    @Mock
    lateinit var caseRequestService: CaseRequestService

    @Mock
    lateinit var sakResource: SakResource

    private lateinit var caseController: CaseController

    @BeforeEach
    fun setUp() {
        caseController = CaseController(caseRequestService)
    }

    @Test
    fun `getCaseTitle returns 200 with case title when case is found`() {
        whenever(sakResource.tittel).thenReturn("Test tittel")
        whenever(caseRequestService.getByMappeId("2023/102")).thenReturn(sakResource)

        val response = caseController.getCaseTitle("2023", "102")

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isNotNull
        assertThat(response.body!!.value).isEqualTo("Test tittel")

        verify(caseRequestService).getByMappeId("2023/102")
        verifyNoMoreInteractions(caseRequestService)
    }

    @Test
    fun `getCaseTitle returns 404 when case is not found`() {
        whenever(caseRequestService.getByMappeId("2023/101")).thenReturn(null)

        assertThatThrownBy { caseController.getCaseTitle("2023", "101") }
            .isInstanceOf(ResponseStatusException::class.java)
            .hasMessageContaining("Case with mappeId=2023/101 could not be found")

        verify(caseRequestService).getByMappeId("2023/101")
        verifyNoMoreInteractions(caseRequestService)
    }
}
