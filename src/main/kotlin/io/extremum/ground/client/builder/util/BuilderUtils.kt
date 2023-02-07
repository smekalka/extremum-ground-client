package io.extremum.ground.client.builder.util

import io.extremum.ground.client.builder.core.outputfield.OutputField
import io.smallrye.graphql.client.core.Argument
import io.smallrye.graphql.client.core.Field
import io.smallrye.graphql.client.impl.core.InputObjectFieldImpl
import io.smallrye.graphql.client.impl.core.InputObjectImpl

internal object BuilderUtils {

    /**
     * Поле ключ-значение.
     * В результирующем запросе будет "fieldName: fieldValue"
     */
    fun property(fieldName: String, fieldValue: Any?): InputObjectFieldImpl =
        InputObjectFieldImpl().apply {
            name = fieldName
            value = fieldValue
        }

    /**
     * Поле со свойствами.
     * В результирующем запросе будет
     * fieldName: {
     *      propertyName: propertyValue
     *      propertyName: propertyValue
     *      ...
     * }
     */
    fun fieldWithProperties(fieldName: String, vararg properties: InputObjectFieldImpl): Argument {
        val input = InputObjectImpl().apply {
            inputObjectFields = properties.asList()
        }
        return Argument.arg(fieldName, input)
    }

    fun Collection<OutputField>.toFields(): List<Field> = this.map { it.toField() }
}