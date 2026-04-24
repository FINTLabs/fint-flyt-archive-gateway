package no.novari.flyt.archive.gateway.resource.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.core.type.TypeReference
import no.novari.fint.model.resource.AbstractCollectionResources

class ResourceCollection : AbstractCollectionResources<Any>() {
    @JsonIgnore
    @Deprecated("Use collection type metadata from the concrete resource collection")
    override fun getTypeReference(): TypeReference<List<Any>> = object : TypeReference<List<Any>>() {}
}
