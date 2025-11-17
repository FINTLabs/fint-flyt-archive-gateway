package no.novari.flyt.archive.gateway.template.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.util.Map;

@Getter
@Builder
@EqualsAndHashCode
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UrlBuilder {

    @NotBlank
    private final String urlTemplate;

    private final Map<String, @NotBlank String> valueRefPerPathParamKey;

    private final Map<String, @NotBlank String> valueRefPerRequestParamKey;

}
