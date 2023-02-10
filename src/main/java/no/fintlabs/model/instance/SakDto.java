package no.fintlabs.model.instance;

import lombok.*;
import no.fintlabs.model.CaseDispatchType;
import no.fintlabs.validation.groups.CaseByIdValidationGroup;
import no.fintlabs.validation.groups.CaseBySearchValidationGroup;
import no.fintlabs.validation.groups.NewCaseValidationGroup;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SakDto {

    @NotNull
    private CaseDispatchType type;

    @NotBlank(groups = CaseByIdValidationGroup.class)
    private String id;

    @NotNull(groups = {NewCaseValidationGroup.class, CaseBySearchValidationGroup.class})
    private NySakDto ny;

}
