package io.extremum.ground.client.builder.impl

import io.extremum.ground.client.builder.constant.BuilderConstants.ID_FIELD_NAME
import io.extremum.ground.client.builder.core.GraphQlUpdateSublistBuilder
import io.extremum.ground.client.builder.util.ValueGraphQlFormatter.format
import io.extremum.model.tools.mapper.GraphQlListUtils.LIST_ELEMENT_FIELD_NAME
import io.extremum.model.tools.mapper.GraphQlListUtils.LIST_FIELD_NAME
import io.extremum.model.tools.mapper.MapperUtils.convertToMap
import io.extremum.sharedmodels.basic.BasicModel
import io.smallrye.graphql.client.core.Argument.arg
import io.smallrye.graphql.client.core.Document
import io.smallrye.graphql.client.core.Document.document
import io.smallrye.graphql.client.core.Field.field
import io.smallrye.graphql.client.core.Operation.operation
import io.smallrye.graphql.client.core.OperationType.MUTATION

/**
 * Добавление в сущность с id [id] сущности в дочерний список.
 */
class GraphQlAddToSublistBuilder<T : BasicModel<*>>
internal constructor(id: Any, entitiesToAdd: List<T>, inputFields: List<String> = listOf()) :
    GraphQlUpdateSublistBuilder(id) {

    private val input: List<Map<String, Any?>>

    init {
        input = entitiesToAdd.map { entity ->
            entity.convertToMap(inputFields)
                .filterValues { it != null }
        }
    }

    override fun getMutationName(): String = "add"

    override fun buildInternal(rootName: String): Document {
        require(sublistMutationName?.isBlank() == false) { "sublist name must be set" }
        require(input.isNotEmpty()) { "input fields must be set" }//todo а пустую модель можно добавить?
        return document(
            operation(
                MUTATION,
                field(
                    rootName,
                    arg(ID_FIELD_NAME, StringBuilder(id.format())),
                ),
                field(
                    "",
                    field(
                        sublistMutationName,
                        arg("input", StringBuilder(input.format())),
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