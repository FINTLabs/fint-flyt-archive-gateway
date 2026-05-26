package no.novari.flyt.archive.gateway.dispatch

import no.novari.flyt.archive.gateway.dispatch.model.CaseDispatchType
import no.novari.flyt.archive.gateway.dispatch.model.instance.ArchiveInstance
import no.novari.flyt.archive.gateway.dispatch.model.instance.JournalpostDto
import no.novari.flyt.archive.gateway.dispatch.model.instance.SakDto
import no.novari.flyt.archive.gateway.dispatch.sak.CaseDispatchService
import no.novari.flyt.archive.gateway.dispatch.sak.result.CaseDispatchResult
import no.novari.flyt.archive.gateway.dispatch.sak.result.CaseSearchResult
import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeaders
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class DispatchServiceTest {
    @Mock
    private lateinit var caseDispatchService: CaseDispatchService

    @Mock
    private lateinit var instanceFlowHeaders: InstanceFlowHeaders

    @Mock
    private lateinit var recordsProcessingService: RecordsProcessingService

    @InjectMocks
    private lateinit var dispatchService: DispatchService

    @Test
    fun `given case type NEW with no journalpost and an accepted dispatch, returns an accepted result with case id`() {
        val sakDto = SakDto.builder().build()
        val archiveInstance =
            ArchiveInstance
                .builder()
                .type(CaseDispatchType.NEW)
                .newCase(sakDto)
                .build()
        whenever(caseDispatchService.dispatch(sakDto)).thenReturn(CaseDispatchResult.accepted("testCaseId"))

        val result = dispatchService.process(instanceFlowHeaders, archiveInstance)

        assertThat(result).isEqualTo(DispatchResult.accepted("testCaseId"))
    }

    @Test
    fun `given case type NEW and an accepted dispatch, calls records processing service and returns its result`() {
        val journalpostDto = mock<JournalpostDto>()
        val sakDto = SakDto.builder().journalpost(listOf(journalpostDto)).build()
        val archiveInstance =
            ArchiveInstance
                .builder()
                .type(CaseDispatchType.NEW)
                .newCase(sakDto)
                .build()
        whenever(caseDispatchService.dispatch(sakDto)).thenReturn(CaseDispatchResult.accepted("testCaseId"))
        whenever(recordsProcessingService.processRecords("testCaseId", true, listOf(journalpostDto)))
            .thenReturn(DispatchResult.accepted("testCaseId"))

        val result = dispatchService.process(instanceFlowHeaders, archiveInstance)

        assertThat(result).isEqualTo(DispatchResult.accepted("testCaseId"))
    }

    @Test
    fun `given case type BY_ID, calls records processing service with newCase=false`() {
        val journalpostDto = mock<JournalpostDto>()
        val archiveInstance =
            ArchiveInstance
                .builder()
                .type(CaseDispatchType.BY_ID)
                .caseId("testCaseId")
                .journalpost(listOf(journalpostDto))
                .build()
        whenever(recordsProcessingService.processRecords("testCaseId", false, listOf(journalpostDto)))
            .thenReturn(DispatchResult.accepted("testCaseId"))

        val result = dispatchService.process(instanceFlowHeaders, archiveInstance)

        assertThat(result).isEqualTo(DispatchResult.accepted("testCaseId"))
    }

    @Test
    fun `given case type BY_SEARCH_OR_NEW with multiple matching cases, returns a declined result`() {
        val journalpostDto = mock<JournalpostDto>()
        val sakDto = SakDto.builder().journalpost(listOf(journalpostDto)).build()
        val archiveInstance =
            ArchiveInstance
                .builder()
                .type(CaseDispatchType.BY_SEARCH_OR_NEW)
                .newCase(sakDto)
                .build()
        whenever(caseDispatchService.findCasesBySearch(archiveInstance))
            .thenReturn(CaseSearchResult.accepted(listOf("caseId1", "caseId2")))

        val result = dispatchService.process(instanceFlowHeaders, archiveInstance)

        assertThat(result).isEqualTo(DispatchResult.declined("Found multiple cases: caseId1, caseId2"))
    }

    @Test
    fun `given case type BY_SEARCH_OR_NEW and no cases found, delegates to processNew`() {
        val sakDto = SakDto.builder().build()
        val archiveInstance =
            ArchiveInstance
                .builder()
                .type(CaseDispatchType.BY_SEARCH_OR_NEW)
                .newCase(sakDto)
                .build()
        whenever(caseDispatchService.findCasesBySearch(archiveInstance))
            .thenReturn(CaseSearchResult.accepted(emptyList()))
        whenever(caseDispatchService.dispatch(sakDto)).thenReturn(CaseDispatchResult.accepted("testCaseId"))

        val result = dispatchService.process(instanceFlowHeaders, archiveInstance)

        assertThat(result).isEqualTo(DispatchResult.accepted("testCaseId"))
    }
}
