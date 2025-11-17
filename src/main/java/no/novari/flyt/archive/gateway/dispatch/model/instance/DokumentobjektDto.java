package no.novari.flyt.archive.gateway.dispatch.model.instance;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.util.Optional;
import java.util.UUID;

@Builder
@Jacksonized
public class DokumentobjektDto {
    private final String variantformat;
    private final String filformat;
    private final String format;
    @JsonProperty(value = "fil")
    @NotNull
    private final UUID fileId;

    public Optional<String> getVariantformat() {
        return Optional.ofNullable(variantformat);
    }

    public Optional<String> getFilformat() {
        return Optional.ofNullable(filformat);
    }

    public Optional<String> getFormat() {
        return Optional.ofNullable(format);
    }

    public Optional<UUID> getFileId() {
        return Optional.ofNullable(fileId);
    }

}
