package no.fintlabs.flyt.gateway.application.archive.template.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Getter
@Builder
@EqualsAndHashCode
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValuePredicate {

    @NotBlank
    @Pattern(regexp = "[^.]*")
    private final String key;

    private final Boolean defined;

    private final String value;

    private final String notValue;

}
