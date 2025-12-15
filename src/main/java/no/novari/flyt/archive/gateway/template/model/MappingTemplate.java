package no.novari.flyt.archive.gateway.template.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@EqualsAndHashCode
@Jacksonized
public class MappingTemplate {

    private final String displayName;

    @NotNull
    @Valid
    private final ObjectTemplate rootObjectTemplate;

}
