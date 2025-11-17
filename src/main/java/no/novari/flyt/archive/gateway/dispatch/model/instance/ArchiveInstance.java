package no.novari.flyt.archive.gateway.dispatch.model.instance;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import no.novari.flyt.archive.gateway.dispatch.model.CaseDispatchType;
import no.novari.flyt.archive.gateway.dispatch.model.validation.groups.CaseByIdValidationGroup;
import no.novari.flyt.archive.gateway.dispatch.model.validation.groups.CaseBySearchValidationGroup;
import no.novari.flyt.archive.gateway.dispatch.model.validation.groups.NewCaseValidationGroup;

import java.util.List;


@Getter
@Builder
@Jacksonized
public class ArchiveInstance {

    @NotNull
    private final CaseDispatchType type;

    @Valid
    @NotNull(groups = CaseBySearchValidationGroup.class)
    private final CaseSearchParametersDto caseSearchParameters;

    @Valid
    @NotNull(groups = {NewCaseValidationGroup.class, CaseBySearchValidationGroup.class})
    private final SakDto newCase;

    @NotBlank(groups = CaseByIdValidationGroup.class)
    private final String caseId;

    @NotBlank(groups = CaseByIdValidationGroup.class)
    private final List<@NotNull @Valid JournalpostDto> journalpost;

}
