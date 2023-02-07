package io.extremum.groundClient.builder.impl

import de.cronn.reflection.util.TypedPropertyGetter
import io.extremum.groundClient.builder.constant.BuilderConstants.ID_FIELD_NAME
import io.extremum.groundClient.builder.core.Action
import io.extremum.groundClient.builder.core.GraphQlBuilder
import io.extremum.groundClient.builder.util.StringUtils.getFieldName
import io.extremum.groundClient.builder.util.ValueGraphQlFormatter.format
import io.extremum.model.tools.mapper.MapperUtils.convertToMap
import io.extremum.model.tools.mapper.MapperUtils.hasData
import io.extremum.sharedmodels.basic.BasicModel
import io.extremum.sharedmodels.descriptor.Descriptor
import io.smallrye.graphql.client.core.Argument.arg
import io.smallrye.graphql.client.core.Document
import io.smallrye.graphql.client.core.Document.document
import io.smallrye.graphql.client.core.Field
import io.smallrye.graphql.client.core.Field.field
import io.smallrye.graphql.client.core.Operation.operation
import io.smallrye.graphql.client.core.OperationType.MUTATION
import kotlin.reflect.KProperty1

/**
 * Для запросов создания и обновления сущностей.
 * Если задан [id], то это обновление, иначе - создание
 */
class GraphQlUpdateBuilder
internal constructor(val id: Any? = null) : GraphQlBuilder() {

    override val action: Action = Action.UPDATE

    /**
     * Поля для применения update.
     * Если не задано, то формируется из [entity] и [inputFields]
     */
    var input: Map<String, Any?>? = null

    var entity: BasicModel<*>? = null

    var inputFields: List<String> = listOf()

    /**
     * Сущность на сохранение изменений [entity].
     * Если нужно сохранить не все поля, то нужные необходимо указать в [inputFields].
     * При изменении сущности (при заданном [id]):
     * - При пустом [inputFields] исключаются как неинформативные: null поля, пустые коллекции, boolean: false.
     * - Поле [ID_FIELD_NAME] исключается по умолчанию.
     * При создании сущности (без [id]):
     * - Передаются только не null поля.
     *
     * Альтернативой изменение сущности можно задать через сравнение двух сущностей [setInputForUpdating]
     */
    fun <T : BasicModel<*>> setInput(
        entity: T,
        inputFields: List<KProperty1<T, Any?>> = listOf()
    ): GraphQlUpdateBuilder {
        this.entity = entity
        this.inputFields = inputFields.map { it.name }
        return this
    }

    fun <T : BasicModel<*>> setInput(entity: T, vararg inputFields: KProperty1<T, Any?>): GraphQlUpdateBuilder {
        return setInput(entity, inputFields.asList())
    }

    fun <T : BasicModel<*>> setInput(entity: T, vararg inputFields: String): GraphQlUpdateBuilder {
        this.entity = entity
        this.inputFields = inputFields.asList()
        return this
    }

    inline fun <reified T : BasicModel<*>> setInput(
        entity: T,
        vararg inputFields: TypedPropertyGetter<T, *>
    ): GraphQlUpdateBuilder {
        this.entity = entity
        this.inputFields = inputFields.asList().map {
            getFieldName(T::class, it)
        }
        return this
    }

    /**
     * Задать изменение сущности через сравнение предыдущей сущности и новой
     */
    inline fun <reified T : BasicModel<*>> setInputForUpdating(prev: T, new: T): GraphQlUpdateBuilder {
        input = formInput(prev, new, T::class.java)
        return this
    }

    override fun buildInternal(rootName: String): Document {
        val inputField =
            if (id == null) {
                getInputToCreate(rootName)
            } else {
                getInputToUpdate(rootName)
            }
        return document(
            operation(
                MUTATION,
                inputField,
                field(
                    "",
                    *buildOutputFields()
                )
            )
        )
    }

    private fun getInputToCreate(rootName: String): Field {
        require(entity != null) { "entity must be set" }
        val input = entity!!.convertToMap(inputFields)
            .filterValues { it.hasData() }
        return field(
            rootName,
            arg("input", StringBuilder(input.format()))
        )
    }

    private fun getInputToUpdate(rootName: String): Field {
        require(input != null || entity != null) { "input or entity must be set" }
        val finalInput = input ?: entity!!.convertToMap(inputFields)
            .filterKeys { it != ID_FIELD_NAME }
            .filter { (key, value) -> key in inputFields || value.hasData() }
        return field(
            rootName,
            arg("id", StringBuilder(id.format())),
            arg("input", StringBuilder(finalInput.format(filterEmpty = false)))
        )
    }

    companion object {
        fun <T : BasicModel<*>> formInput(prev: T, new: T, clazz: Class<*>): Map<String, Any?> {
            val fields = allFields(clazz)
            val mapNotNull = fields.mapNotNull { field ->
                val fieldName = field.name
                val fieldType = field.type
                field.isAccessible = true
                val newValue = field.get(new)
                val prevValue = field.get(prev)
                if (prevValue == null && newValue == null) {
                    null
                } else if ((prevValue == null) != (newValue == null)) {
                    fieldName to newValue
                } else {
                    if (BasicModel::class.java.isAssignableFrom(fieldType)) {
                        fieldName to formInput(prevValue as BasicModel<*>, newValue as BasicModel<*>, fieldType)
                    } else if (fieldType == Descriptor::class.java) {
                        if ((prevValue as Descriptor).externalId == (newValue as Descriptor).externalId) {
                            null
                        } else {
                            fieldName to newValue
                        }
                    } else {
                        if (prevValue == newValue) {
                            null
                        } else {
                            fieldName to newValue
                        }
                    }
                }
            }
            return mapNotNull.toMap()
        }

        private fun allFields(clazz: Class<out Any>): List<java.lang.reflect.Field> {
            val fields = clazz.declaredFields.toList()
            val superclass = clazz.superclass
            return if (superclass == null) fields else fields + allFields(superclass)
        }
    }
}