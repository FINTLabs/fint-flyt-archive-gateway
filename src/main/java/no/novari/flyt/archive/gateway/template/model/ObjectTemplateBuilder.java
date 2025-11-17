package no.novari.flyt.archive.gateway.template.model;

import java.util.ArrayList;
import java.util.Collection;

public class ObjectTemplateBuilder {
    private int nextOrderValue = 0;
    private final Collection<ElementTemplate<ValueTemplate>> valueTemplates = new ArrayList<>();
    private final Collection<ElementTemplate<SelectableValueTemplate>> selectableValueTemplates = new ArrayList<>();
    private final Collection<ElementTemplate<CollectionTemplate<ValueTemplate>>> valueCollectionTemplates = new ArrayList<>();
    private final Collection<ElementTemplate<ObjectTemplate>> objectTemplates = new ArrayList<>();
    private final Collection<ElementTemplate<CollectionTemplate<ObjectTemplate>>> objectCollectionTemplates = new ArrayList<>();

    public ObjectTemplateBuilder addTemplate(ElementConfig elementConfig, ValueTemplate template) {
        return addTemplate(valueTemplates, elementConfig, template);
    }

    public ObjectTemplateBuilder addTemplate(ElementConfig elementConfig, SelectableValueTemplate template) {
        return addTemplate(selectableValueTemplates, elementConfig, template);
    }

    public ObjectTemplateBuilder addTemplate(ElementConfig elementConfig, ObjectTemplate template) {
        return addTemplate(objectTemplates, elementConfig, template);
    }

    public ObjectTemplateBuilder addCollectionTemplate(ElementConfig elementConfig, ValueTemplate elementTemplate) {
        return addTemplate(
                valueCollectionTemplates,
                elementConfig,
                CollectionTemplate
                        .<ValueTemplate>builder()
                        .elementTemplate(elementTemplate)
                        .build()
        );
    }

    public ObjectTemplateBuilder addCollectionTemplate(ElementConfig elementConfig, ObjectTemplate elementTemplate) {
        return addTemplate(
                objectCollectionTemplates,
                elementConfig,
                CollectionTemplate
                        .<ObjectTemplate>builder()
                        .elementTemplate(elementTemplate)
                        .build()
        );
    }

    private <T> ObjectTemplateBuilder addTemplate(
            Collection<ElementTemplate<T>> collection,
            ElementConfig elementConfig,
            T template
    ) {
        collection.add(
                ElementTemplate
                        .<T>builder()
                        .order(nextOrderValue++)
                        .elementConfig(elementConfig)
                        .template(template)
                        .build()
        );
        return this;
    }

    public ObjectTemplate build() {
        return new ObjectTemplate(
                valueTemplates,
                selectableValueTemplates,
                valueCollectionTemplates,
                objectTemplates,
                objectCollectionTemplates
        );
    }

}
