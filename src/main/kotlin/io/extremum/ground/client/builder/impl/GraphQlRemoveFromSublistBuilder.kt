package io.extremum.ground.client.builder.impl

import io.extremum.ground.client.builder.constant.BuilderConstants.ID_FIELD_NAME
import io.extremum.ground.client.builder.core.GraphQlUpdateSublistBuilder
import io.extremum.ground.client.builder.util.ValueGraphQlFormatter.format
import io.extremum.model.tools.mapper.GraphQlListUtils.LIST_ELEMENT_FIELD_NAME
import io.extremum.model.tools.mapper.GraphQlListUtils.LIST_FIELD_NAME
import io.smallrye.graphql.client.core.Argument.arg
import io.smallrye.graphql.client.core.Document
import io.smallrye.graphql.client.core.Document.document
import io.smallrye.graphql.client.core.Field.field
import io.smallrye.graphql.client.core.Operation.operation
import io.smallrye.graphql.client.core.OperationType.MUTATION

/**
 * Удаление в сущности с id [id] в дочернем списке элемента с id [idToRemove].
 */
class GraphQlRemoveFromSublistBuilder
internal constructor(id: Any, private val idToRemove: Any) : GraphQlUpdateSublistBuilder(id) {

    override fun getMutationName(): String = "remove"

    override fun buildInternal(rootName: String): Document {
        require(sublistMutationName?.isBlank() == false) { "sublist name must be set" }
        return document(
            operation(
                MUTATION,
                field(
                    rootName,
                    arg("id", StringBuilder(id.format())),
                ),
                field(
                    "",
                    field(
                        sublistMutationName,
                        arg("input", StringBuilder(mapOf(ID_FIELD_NAME to idToRemove).format())),
                        *buildPagingAndFilter().toTypedArray()
                    ),
                    field(
                        "",
                        field(
                            LIST_FIELD_NAME,
                            field(
                                LIST_ELEMENT_FIELD_NAME,
                                *buildOutputFields()
                            )
                        )
                    )
                )
            )
        )
    }
}