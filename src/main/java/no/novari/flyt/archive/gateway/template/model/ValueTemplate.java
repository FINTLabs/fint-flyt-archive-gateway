package no.novari.flyt.archive.gateway.template.model;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValueTemplate {

    public enum Type {
        STRING, DYNAMIC_STRING, FILE, BOOLEAN
    }

    @NotNull
    private final Type type;

    @Valid
    private final UrlBuilder search;

}
