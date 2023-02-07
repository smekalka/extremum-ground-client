package io.extremum.groundClient.builder.impl

import io.extremum.groundClient.builder.core.Action
import io.extremum.groundClient.builder.core.GraphQlBuilder
import io.extremum.groundClient.builder.util.ValueGraphQlFormatter.format
import io.smallrye.graphql.client.core.Argument.arg
import io.smallrye.graphql.client.core.Document
import io.smallrye.graphql.client.core.Document.document
import io.smallrye.graphql.client.core.Field.field
import io.smallrye.graphql.client.core.Operation.operation

class GraphQlGetByIdBuilder
internal constructor(val id: Any) : GraphQlBuilder() {

    override val action: Action = Action.GET_BY_ID

    override fun buildInternal(rootName: String): Document {
        return document(
            operation(
                field(
                    rootName,
                    arg("id", StringBuilder(id.format())),
                ),
                field(
                    "",
                    *buildOutputFields()
                )
            )
        )
    }
}