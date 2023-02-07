package io.extremum.groundClient.builder.impl

import io.extremum.groundClient.builder.core.Action
import io.extremum.groundClient.builder.core.GraphQlBuilder
import io.extremum.groundClient.builder.util.ValueGraphQlFormatter.format
import io.smallrye.graphql.client.core.Argument.arg
import io.smallrye.graphql.client.core.Document
import io.smallrye.graphql.client.core.Document.document
import io.smallrye.graphql.client.core.Field.field
import io.smallrye.graphql.client.core.Operation.operation
import io.smallrye.graphql.client.core.OperationType.MUTATION

class GraphQlRemoveBuilder
internal constructor(val id: Any) : GraphQlBuilder() {

    override val action: Action = Action.REMOVE

    override fun buildInternal(rootName: String): Document {
        return document(
            operation(
                MUTATION,
                field(
                    "delete",
                    arg("id", StringBuilder(id.format())),
                )
            )
        )
    }
}