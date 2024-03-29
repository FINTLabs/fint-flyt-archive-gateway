package no.fintlabs.flyt.gateway.application.archive.template.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import no.fintlabs.flyt.gateway.application.archive.template.validation.UniqueKeys;
import no.fintlabs.flyt.gateway.application.archive.template.validation.UniqueOrders;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

@UniqueKeys
@UniqueOrders
@Getter
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ObjectTemplate {

    public static ObjectTemplateBuilder builder() {
        return new ObjectTemplateBuilder();
    }

    @JsonCreator
    public ObjectTemplate(
            @JsonProperty("valueTemplates") Collection<ElementTemplate<ValueTemplate>> valueTemplates,
            @JsonProperty("selectableValueTemplates") Collection<ElementTemplate<SelectableValueTemplate>> selectableValueTemplates,
            @JsonProperty("valueCollectionTemplates") Collection<ElementTemplate<CollectionTemplate<ValueTemplate>>> valueCollectionTemplates,
            @JsonProperty("objectTemplates") Collection<ElementTemplate<ObjectTemplate>> objectTemplates,
            @JsonProperty("objectCollectionTemplates") Collection<ElementTemplate<CollectionTemplate<ObjectTemplate>>> objectCollectionTemplates
    ) {
        this.valueTemplates = Optional.ofNullable(valueTemplates).orElse(new ArrayList<>());
        this.selectableValueTemplates = Optional.ofNullable(selectableValueTemplates).orElse(new ArrayList<>());
        this.valueCollectionTemplates = Optional.ofNullable(valueCollectionTemplates).orElse(new ArrayList<>());
        this.objectTemplates = Optional.ofNullable(objectTemplates).orElse(new ArrayList<>());
        this.objectCollectionTemplates = Optional.ofNullable(objectCollectionTemplates).orElse(new ArrayList<>());
    }

    private final Collection<@NotNull @Valid ElementTemplate<ValueTemplate>> valueTemplates;
    private final Collection<@NotNull @Valid ElementTemplate<SelectableValueTemplate>> selectableValueTemplates;
    private final Collection<@NotNull @Valid ElementTemplate<CollectionTemplate<ValueTemplate>>> valueCollectionTemplates;
    private final Collection<@NotNull @Valid ElementTemplate<ObjectTemplate>> objectTemplates;
    private final Collection<@NotNull @Valid ElementTemplate<CollectionTemplate<ObjectTemplate>>> objectCollectionTemplates;

}
