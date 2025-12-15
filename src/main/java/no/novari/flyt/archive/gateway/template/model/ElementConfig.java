package no.novari.flyt.archive.gateway.template.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
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
public class ElementConfig {

    @NotBlank
    @Pattern(regexp = "[^.]*")
    private final String key;

    @NotBlank
    private final String displayName;

    private final String description;

    @Valid
    private final Dependency showDependency;

    @Valid
    private final Dependency enableDependency;

}
