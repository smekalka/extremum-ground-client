package io.extremum.ground.client.builder.core.outputfield

import io.smallrye.graphql.client.core.Field
import kotlin.reflect.KProperty1

class OutputFieldKPr<T, V> internal constructor(
    private val name: KProperty1<T, Any?>,
    private val nested: List<OutputFieldKPr<V, out Any?>> = listOf(),
) : OutputField() {

    override fun fieldName(): String = name.name

    override fun nestedFields(): List<Field> = nested.map { it.toField() }

    fun toOutputFieldStr(): OutputFieldStr = OutputFieldStr(name.name, nested = nested.map { it.toOutputFieldStr() })
}