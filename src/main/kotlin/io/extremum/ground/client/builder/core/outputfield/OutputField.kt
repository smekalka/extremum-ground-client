package io.extremum.ground.client.builder.core.outputfield

import io.extremum.ground.client.builder.constant.BuilderConstants.ID_FIELD_NAME
import io.extremum.ground.client.builder.core.Pageable
import io.extremum.ground.client.builder.core.PagingAndSortingRequest
import io.extremum.model.tools.mapper.GraphQlListUtils.LIST_ELEMENT_FIELD_NAME
import io.extremum.model.tools.mapper.GraphQlListUtils.LIST_FIELD_NAME
import io.smallrye.graphql.client.core.Field

abstract class OutputField : Pageable {

    override var paging: PagingAndSortingRequest? = null

    override var filter: String? = null

    protected abstract fun fieldName(): String

    protected abstract fun nestedFields(): List<Field>

    fun toField(): Field =
        if (paging == null) {
            Field.field(fieldName(), *nestedFields().toTypedArray())
        } else {
            val nestedFields = nestedFields().ifEmpty {
                listOf(Field.field(ID_FIELD_NAME))
            }
            Field.field(
                fieldName(),
                buildPagingAndFilter(),
                Field.field(
                    LIST_FIELD_NAME,
                    Field.field(
                        LIST_ELEMENT_FIELD_NAME,
                        *nestedFields.toTypedArray()
                    )
                )
            )
        }
}