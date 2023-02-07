package io.extremum.ground.client.builder.core.outputfield

import io.smallrye.graphql.client.core.Field

class OutputFieldStr(
    private val name: String,
    private val nested: List<OutputFieldStr> = listOf(),
) : OutputField() {

    override fun fieldName(): String = name

    override fun nestedFields(): List<Field> = nested.map { it.toField() }
}