package no.novari.flyt.archive.gateway.template.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

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
