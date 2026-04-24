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
) {
    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder {
        private var type: CaseDispatchType? = null
        private var caseSearchParameters: CaseSearchParametersDto? = null
        private var newCase: SakDto? = null
        private var caseId: String? = null
        private var journalpost: List<JournalpostDto>? = null

        fun type(type: CaseDispatchType?) = apply { this.type = type }

        fun caseSearchParameters(caseSearchParameters: CaseSearchParametersDto?) =
            apply {
                this.caseSearchParameters = caseSearchParameters
            }

        fun newCase(newCase: SakDto?) = apply { this.newCase = newCase }

        fun caseId(caseId: String?) = apply { this.caseId = caseId }

        fun journalpost(journalpost: List<JournalpostDto>?) = apply { this.journalpost = journalpost }

        fun build() =
            ArchiveInstance(
                type = type,
                caseSearchParameters = caseSearchParameters,
                newCase = newCase,
                caseId = caseId,
                journalpost = journalpost,
            )
    }
}
