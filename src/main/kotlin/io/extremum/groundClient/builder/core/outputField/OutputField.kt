package io.extremum.groundClient.builder.core.outputField

import io.extremum.groundClient.builder.constant.BuilderConstants.ID_FIELD_NAME
import io.extremum.groundClient.builder.core.Pageable
import io.extremum.groundClient.builder.core.PagingAndSortingRequest
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