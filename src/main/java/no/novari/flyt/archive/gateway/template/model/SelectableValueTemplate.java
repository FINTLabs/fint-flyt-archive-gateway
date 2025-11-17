package no.novari.flyt.archive.gateway.template.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import no.novari.flyt.archive.gateway.template.validation.AtLeastOneSelectable;

import java.util.Collection;

@AtLeastOneSelectable
@Getter
@Builder
@EqualsAndHashCode
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SelectableValueTemplate {

    public enum Type {
        DYNAMIC_STRING_OR_SEARCH_SELECT, SEARCH_SELECT, DROPDOWN
    }

    @NotNull
    private final Type type;

    private final Collection<@Valid Selectable> selectables;

    private final Collection<@Valid UrlBuilder> selectablesSources;

}
