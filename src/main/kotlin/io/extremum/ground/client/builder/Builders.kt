package io.extremum.ground.client.builder

import de.cronn.reflection.util.TypedPropertyGetter
import io.extremum.ground.client.builder.core.PagingAndSortingRequest
import io.extremum.ground.client.builder.core.setSublistMutationName
import io.extremum.ground.client.builder.impl.GraphQlAddToSublistBuilder
import io.extremum.ground.client.builder.impl.GraphQlGetByIdBuilder
import io.extremum.ground.client.builder.impl.GraphQlQueryBuilder
import io.extremum.ground.client.builder.impl.GraphQlRemoveBuilder
import io.extremum.ground.client.builder.impl.GraphQlRemoveFromSublistBuilder
import io.extremum.ground.client.builder.impl.GraphQlUpdateBuilder
import io.extremum.ground.client.builder.util.StringUtils.getFieldName
import io.extremum.sharedmodels.basic.BasicModel
import kotlin.reflect.KProperty1

object Builders {

    /**
     * Запрос списка определенной сущности.
     */
    fun query(
        paging: PagingAndSortingRequest = PagingAndSortingRequest(),
        filter: String? = null
    ): GraphQlQueryBuilder =
        GraphQlQueryBuilder(paging, filter)

    /**
     * Получения сущности по [id].
     */
    fun getById(id: Any): GraphQlGetByIdBuilder = GraphQlGetByIdBuilder(id)

    /**
     * Создание/редактирование сущности.
     * Если задан [id], то это редактирование, иначе - создание.
     */
    fun update(id: Any? = null): GraphQlUpdateBuilder = GraphQlUpdateBuilder(id)

    /**
     * Удаление сущности с id [id].
     */
    fun remove(id: Any): GraphQlRemoveBuilder = GraphQlRemoveBuilder(id)

    /**
     * Добавление в сущность с id [id]  в дочерний список [sublistFieldName] сущности [entitiesToAdd].
     * Передаются только не null поля добавляемой сущности.
     * Если нужно сохранить не все поля, то нужные необходимо указать в [inputFields].
     */
    fun <T : BasicModel<*>> addToSublist(
        id: Any,
        sublistFieldName: String,
        entitiesToAdd: List<T>,
        inputFields: List<String> = listOf()
    ): GraphQlAddToSublistBuilder<T> =
        GraphQlAddToSublistBuilder(id, entitiesToAdd, inputFields)
            .setSublistMutationName(sublistFieldName)

    fun <T : BasicModel<*>> addToSublist(
        id: Any,
        sublistField: KProperty1<out BasicModel<*>, Any?>,
        entitiesToAdd: List<T>,
        inputFields: List<String> = listOf()
    ): GraphQlAddToSublistBuilder<T> =
        addToSublist(id, sublistField.name, entitiesToAdd, inputFields)

    inline fun <reified T : BasicModel<*>, V : BasicModel<*>> addToSublist(
        id: Any,
        sublistFieldGetter: TypedPropertyGetter<T, *>,
        entitiesToAdd: List<V>,
        inputFields: List<String> = listOf()
    ): GraphQlAddToSublistBuilder<V> =
        addToSublist(id, getFieldName(T::class, sublistFieldGetter), entitiesToAdd, inputFields)

    inline fun <reified T : BasicModel<*>, V : BasicModel<*>> addToSublist(
        id: Any,
        sublistFieldGetter: TypedPropertyGetter<T, *>,
        entityToAdd: V
    ): GraphQlAddToSublistBuilder<V> =
        addToSublist(id, getFieldName(T::class, sublistFieldGetter), listOf(entityToAdd))

    fun <T : BasicModel<*>> addToSublist(
        id: Any,
        sublistFieldName: String,
        entityToAdd: T,
        inputFields: List<String> = listOf()
    ): GraphQlAddToSublistBuilder<T> =
        GraphQlAddToSublistBuilder(id, listOf(entityToAdd), inputFields)
            .setSublistMutationName(sublistFieldName)

    /**
     * Удаление в сущности с id [id] в дочернем списке [sublistFieldName] элемента с id [idToRemove].
     */
    fun removeFromSublist(id: Any, sublistFieldName: String, idToRemove: Any)
            : GraphQlRemoveFromSublistBuilder =
        GraphQlRemoveFromSublistBuilder(id, idToRemove)
            .setSublistMutationName(sublistFieldName)

    fun removeFromSublist(id: Any, sublistField: KProperty1<out BasicModel<*>, Any?>, idToRemove: Any)
            : GraphQlRemoveFromSublistBuilder =
        removeFromSublist(id, sublistField.name, idToRemove)

    inline fun <reified T : BasicModel<*>> removeFromSublist(
        id: Any,
        sublistFieldGetter: TypedPropertyGetter<T, *>,
        idToRemove: Any
    ): GraphQlRemoveFromSublistBuilder =
        removeFromSublist(id, getFieldName(T::class, sublistFieldGetter), idToRemove)
}