package no.fintlabs.flyt.gateway.application.archive.resource.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import no.fint.model.resource.AbstractCollectionResources;

import java.util.List;

public class ResourceCollection extends AbstractCollectionResources<Object> {
    @Override
    @JsonIgnore
    @Deprecated
    public TypeReference<List<Object>> getTypeReference() {
        return new TypeReference<>() {
        };
    }

    public ResourceCollection() {
    }
}
