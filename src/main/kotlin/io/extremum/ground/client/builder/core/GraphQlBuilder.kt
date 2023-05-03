package io.extremum.ground.client.builder.core

import io.extremum.ground.client.builder.constant.BuilderConstants.ID_FIELD_NAME
import io.extremum.ground.client.builder.core.outputfield.OutputField
import io.extremum.ground.client.builder.core.outputfield.OutputFieldUtils.allFields
import io.extremum.ground.client.builder.tx.TxAction
import io.extremum.ground.client.builder.tx.WithTxAction
import io.extremum.ground.client.builder.util.BuilderUtils.toFields
import io.extremum.ground.client.builder.util.StringUtils.escape
import io.smallrye.graphql.client.core.Document
import io.smallrye.graphql.client.core.Field
import kotlin.reflect.KClass

abstract class GraphQlBuilder : WithTxAction {

    abstract val action: Action

    /**
     * Поля, необходимые для включения в выходной результат запроса.
     * [ID_FIELD_NAME] добавляется в любом случае.
     */
    var outputFields: MutableSet<Field> = mutableSetOf()

    override var txAction: TxAction? = null

    fun build(rootName: String): String = """{
        "query": "${buildInternal(rootName).build().escape()}"
    }"""

    protected abstract fun buildInternal(rootName: String): Document

    open fun getRootName(collectionName: String): String = collectionName

    protected fun buildOutputFields(): Array<Field> =
        if (outputFields.isEmpty()) {
            arrayOf(Field.field(ID_FIELD_NAME))
        } else {
            val result = outputFields.toTypedArray()
            if (result.find { it.name == ID_FIELD_NAME } == null) {
                result + Field.field(ID_FIELD_NAME)
            } else {
                result
            }
        }
}

fun <T : GraphQlBuilder> T.setOutputFields(outputFields: List<OutputField>): T {
    require(outputFields.isNotEmpty()) { "Output fields must not be empty" }
    this.outputFields = outputFields.toFields().toMutableSet()
    return this
}

fun <T : GraphQlBuilder> T.setOutputFields(vararg outputFields: OutputField): T = setOutputFields(outputFields.asList())

fun <T : GraphQlBuilder> T.addOutputFields(vararg outputFields: OutputField): T {
    this.outputFields.addAll(outputFields.toList().toFields())
    return this
}

/**
 * В [GraphQlBuilder.outputFields] установить все поля из заданной модели, в том числе вложенные поля.
 * За исключением полей со списками и полей того же типа, что и сама модель (например, User -> User на любом из уровней).
 * При зацикливании на большем уровне вложенности не применима (например, User -> Role -> User)
 */
fun <T : GraphQlBuilder> T.setAllOutputFields(clazz: Class<*>): T = setOutputFields(allFields(clazz))