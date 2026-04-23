package no.novari.flyt.archive.gateway.template.model

class ObjectTemplateBuilder {
    private var nextOrderValue = 0
    private val valueTemplates = mutableListOf<ElementTemplate<ValueTemplate>>()
    private val selectableValueTemplates = mutableListOf<ElementTemplate<SelectableValueTemplate>>()
    private val valueCollectionTemplates = mutableListOf<ElementTemplate<CollectionTemplate<ValueTemplate>>>()
    private val objectTemplates = mutableListOf<ElementTemplate<ObjectTemplate>>()
    private val objectCollectionTemplates = mutableListOf<ElementTemplate<CollectionTemplate<ObjectTemplate>>>()

    fun addTemplate(
        elementConfig: ElementConfig,
        template: ValueTemplate,
    ): ObjectTemplateBuilder {
        return addTemplate(valueTemplates, elementConfig, template)
    }

    fun addTemplate(
        elementConfig: ElementConfig,
        template: SelectableValueTemplate,
    ): ObjectTemplateBuilder {
        return addTemplate(selectableValueTemplates, elementConfig, template)
    }

    fun addTemplate(
        elementConfig: ElementConfig,
        template: ObjectTemplate,
    ): ObjectTemplateBuilder {
        return addTemplate(objectTemplates, elementConfig, template)
    }

    fun addCollectionTemplate(
        elementConfig: ElementConfig,
        elementTemplate: ValueTemplate,
    ): ObjectTemplateBuilder {
        return addTemplate(
            valueCollectionTemplates,
            elementConfig,
            CollectionTemplate.builder<ValueTemplate>().elementTemplate(elementTemplate).build(),
        )
    }

    fun addCollectionTemplate(
        elementConfig: ElementConfig,
        elementTemplate: ObjectTemplate,
    ): ObjectTemplateBuilder {
        return addTemplate(
            objectCollectionTemplates,
            elementConfig,
            CollectionTemplate.builder<ObjectTemplate>().elementTemplate(elementTemplate).build(),
        )
    }

    private fun <T> addTemplate(
        collection: MutableCollection<ElementTemplate<T>>,
        elementConfig: ElementConfig,
        template: T,
    ): ObjectTemplateBuilder {
        collection +=
            ElementTemplate
                .builder<T>()
                .order(nextOrderValue++)
                .elementConfig(elementConfig)
                .template(template)
                .build()
        return this
    }

    fun build() =
        ObjectTemplate(
            valueTemplates = valueTemplates,
            selectableValueTemplates = selectableValueTemplates,
            valueCollectionTemplates = valueCollectionTemplates,
            objectTemplates = objectTemplates,
            objectCollectionTemplates = objectCollectionTemplates,
        )
}
