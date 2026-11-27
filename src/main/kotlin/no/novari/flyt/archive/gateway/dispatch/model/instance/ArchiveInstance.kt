package no.novari.flyt.archive.gateway.dispatch.model.instance

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import no.novari.flyt.archive.gateway.dispatch.model.CaseDispatchType
import no.novari.flyt.archive.gateway.dispatch.model.validation.groups.CaseByIdValidationGroup
import no.novari.flyt.archive.gateway.dispatch.model.validation.groups.CaseBySearchValidationGroup
import no.novari.flyt.archive.gateway.dispatch.model.validation.groups.NewCaseValidationGroup

data class ArchiveInstance(
    @field:NotNull
    val type: CaseDispatchType? = null,
    @field:Valid
    @field:NotNull(groups = [CaseBySearchValidationGroup::class])
    val caseSearchParameters: CaseSearchParametersDto? = null,
    @field:Valid
    @field:NotNull(groups = [NewCaseValidationGroup::class, CaseBySearchValidationGroup::class])
    val newCase: SakDto? = null,
    @field:NotBlank(groups = [CaseByIdValidationGroup::class])
    val caseId: String? = null,
    @field:NotBlank(groups = [CaseByIdValidationGroup::class])
    val journalpost: List<
        @NotNull @Valid
        JournalpostDto,
    >? = null,
)
