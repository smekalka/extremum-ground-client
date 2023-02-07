package io.extremum.groundClient.builder.impl

import io.extremum.groundClient.builder.core.Action
import io.extremum.groundClient.builder.core.GraphQlBuilder
import io.extremum.groundClient.builder.core.Pageable
import io.extremum.groundClient.builder.core.PagingAndSortingRequest
import io.extremum.model.tools.mapper.GraphQlListUtils.LIST_ELEMENT_FIELD_NAME
import io.extremum.model.tools.mapper.GraphQlListUtils.LIST_FIELD_NAME
import io.smallrye.graphql.client.core.Document
import io.smallrye.graphql.client.core.Document.document
import io.smallrye.graphql.client.core.Field.field
import io.smallrye.graphql.client.core.Operation.operation

class GraphQlQueryBuilder
internal constructor(paging: PagingAndSortingRequest, filter: String? = null) : GraphQlBuilder(), Pageable {

    override val action: Action = Action.QUERY

    override var paging: PagingAndSortingRequest? = PagingAndSortingRequest()

    override var filter: String? = null

    init {
        this.paging = paging
        this.filter = filter
    }

    override fun buildInternal(rootName: String): Document =
        document(
            operation(
                field(
                    rootName,
                    buildPagingAndFilter()
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

    override fun getRootName(collectionName: String): String = "${collectionName}s"
}