package no.novari.flyt.archive.gateway.dispatch

import no.novari.flyt.archive.gateway.dispatch.model.CaseDispatchType
import no.novari.flyt.archive.gateway.dispatch.model.instance.ArchiveInstance
import no.novari.flyt.archive.gateway.dispatch.model.instance.JournalpostDto
import no.novari.flyt.archive.gateway.dispatch.model.instance.SakDto
import no.novari.flyt.archive.gateway.dispatch.sak.CaseDispatchService
import no.novari.flyt.archive.gateway.dispatch.sak.result.CaseDispatchResult
import no.novari.flyt.archive.gateway.dispatch.sak.result.CaseSearchResult
import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeaders
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

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
    fun givenCaseTypeNewAndAcceptedCaseDispatchWithNoJournalpostShouldReturnAcceptedResultWithCaseId() {
        val sakDto = SakDto.builder().build()
        val archiveInstance =
            ArchiveInstance
                .builder()
                .type(CaseDispatchType.NEW)
                .newCase(sakDto)
                .build()
        whenever(caseDispatchService.dispatch(sakDto)).thenReturn(Mono.just(CaseDispatchResult.accepted("testCaseId")))

        StepVerifier
            .create(dispatchService.process(instanceFlowHeaders, archiveInstance))
            .expectNext(DispatchResult.accepted("testCaseId"))
            .verifyComplete()
    }

    @Test
    fun givenCaseTypeNewAndAcceptedCaseDispatchShouldCallRecordsProcessingServiceAndReturnResult() {
        val journalpostDto = mock<JournalpostDto>()
        val sakDto = SakDto.builder().journalpost(listOf(journalpostDto)).build()
        val archiveInstance =
            ArchiveInstance
                .builder()
                .type(CaseDispatchType.NEW)
                .newCase(sakDto)
                .build()
        whenever(caseDispatchService.dispatch(sakDto)).thenReturn(Mono.just(CaseDispatchResult.accepted("testCaseId")))
        whenever(recordsProcessingService.processRecords("testCaseId", true, listOf(journalpostDto)))
            .thenReturn(Mono.just(DispatchResult.accepted("testCaseId")))

        StepVerifier
            .create(dispatchService.process(instanceFlowHeaders, archiveInstance))
            .expectNext(DispatchResult.accepted("testCaseId"))
            .verifyComplete()
    }

    @Test
    fun givenCaseTypeByIdShouldCallRecordsProcessingServiceWithNewCaseFalse() {
        val journalpostDto = mock<JournalpostDto>()
        val archiveInstance =
            ArchiveInstance
                .builder()
                .type(CaseDispatchType.BY_ID)
                .caseId("testCaseId")
                .journalpost(listOf(journalpostDto))
                .build()
        whenever(recordsProcessingService.processRecords("testCaseId", false, listOf(journalpostDto)))
            .thenReturn(Mono.just(DispatchResult.accepted("testCaseId")))

        StepVerifier
            .create(dispatchService.process(instanceFlowHeaders, archiveInstance))
            .expectNext(DispatchResult.accepted("testCaseId"))
            .verifyComplete()
    }

    @Test
    fun givenCaseTypeBySearchOrNewWithMultipleCasesShouldReturnDeclinedResult() {
        val journalpostDto = mock<JournalpostDto>()
        val sakDto = SakDto.builder().journalpost(listOf(journalpostDto)).build()
        val archiveInstance =
            ArchiveInstance
                .builder()
                .type(CaseDispatchType.BY_SEARCH_OR_NEW)
                .newCase(sakDto)
                .build()
        whenever(caseDispatchService.findCasesBySearch(archiveInstance))
            .thenReturn(Mono.just(CaseSearchResult.accepted(listOf("caseId1", "caseId2"))))

        StepVerifier
            .create(dispatchService.process(instanceFlowHeaders, archiveInstance))
            .expectNext(DispatchResult.declined("Found multiple cases: caseId1, caseId2"))
            .verifyComplete()
    }

    @Test
    fun givenCaseTypeBySearchOrNewAndNoCasesFoundShouldDelegateToProcessNew() {
        val sakDto = SakDto.builder().build()
        val archiveInstance =
            ArchiveInstance
                .builder()
                .type(CaseDispatchType.BY_SEARCH_OR_NEW)
                .newCase(sakDto)
                .build()
        whenever(
            caseDispatchService.findCasesBySearch(archiveInstance),
        ).thenReturn(Mono.just(CaseSearchResult.accepted(emptyList())))
        whenever(caseDispatchService.dispatch(sakDto)).thenReturn(Mono.just(CaseDispatchResult.accepted("testCaseId")))

        StepVerifier
            .create(dispatchService.process(instanceFlowHeaders, archiveInstance))
            .expectNext(DispatchResult.accepted("testCaseId"))
            .verifyComplete()
    }
}
