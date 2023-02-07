package io.extremum.groundClient.client

import io.extremum.groundClient.builder.Builders
import io.extremum.groundClient.builder.Builders.remove
import io.extremum.groundClient.builder.core.Action
import io.extremum.groundClient.builder.core.GraphQlUpdateSublistBuilder
import io.extremum.groundClient.builder.core.PagingAndSortingRequest
import io.extremum.groundClient.builder.core.setAllOutputFields
import io.extremum.groundClient.builder.impl.GraphQlGetByIdBuilder
import io.extremum.groundClient.builder.impl.GraphQlQueryBuilder
import io.extremum.groundClient.builder.impl.GraphQlRemoveBuilder
import io.extremum.groundClient.builder.impl.GraphQlUpdateBuilder
import io.extremum.groundClient.builder.tx.TxId
import io.extremum.groundClient.builder.tx.WithTxAction
import io.extremum.groundClient.builder.tx.inTx
import io.extremum.groundClient.builder.util.StringUtils.classNameShort
import io.extremum.model.tools.mapper.MapperUtils.convertValue
import io.extremum.sharedmodels.basic.BasicModel
import java.util.UUID.randomUUID

class GroundApiClient(url: String, headers: Map<String, String>) {

    constructor(url: String) : this(url, mapOf())

    val apiQueryExecutor: ApiQueryExecutor

    init {
        apiQueryExecutor = ApiQueryExecutor(url, headers, xAppId = "0")
    }

    fun updateHeaders(headers: Map<String, String>) {
        apiQueryExecutor.updateHeaders(headers)
    }

    /**
     * Запрос по сущностям определенного типа.
     */
    suspend inline fun <reified T : BasicModel<*>> query(builder: GraphQlQueryBuilder): Response<List<T>> {
        val (response, status, txId, action) = apiQueryExecutor.execute<T, QueryResponse>(builder)
        val responseValue = if (response == null) {
            // это случай, когда задали несуществующую транзакцию в inTx
            listOf()
        } else {
            response.edges?.mapNotNull { it.node?.convertValue<T>() } ?: listOf()
        }
        return Response(value = responseValue, status = status, txId = txId, action = action)
    }

    /**
     * Упрощенный запрос по сущностям определенного типа.
     * В output fields включаются все поля сущности.
     * Если необходимо задать какие-то настройки, нужно использовать [query] с заполненным [GraphQlQueryBuilder].
     */
    suspend inline fun <reified T : BasicModel<*>> query(
        paging: PagingAndSortingRequest = PagingAndSortingRequest(),
        filter: String? = null,
        inTxId: TxId? = null
    ): List<T> {
        val response = query<T>(
            Builders.query(paging, filter)
                .setAllOutputFields(T::class)
                .inTx(inTxId)
        )
        return response.validateStatusAndValueNotNull(classNameShort<T>())
    }

    /**
     * Создание/изменение сущности.
     */
    suspend inline fun <reified T : BasicModel<*>> update(builder: GraphQlUpdateBuilder): Response<T> =
        apiQueryExecutor.execute<T, T>(builder)

    /**
     * Упрощенное создание сущности [value].
     * В output fields включаются все поля сущности.
     * Если необходимо задать какие-то настройки при создании, нужно использовать [update] с заполненным [GraphQlUpdateBuilder].
     */
    suspend inline fun <reified T : BasicModel<*>> create(value: T, inTxId: TxId? = null): T {
        val clazz = T::class
        val builder = Builders.update()
            .setInput(value)
            .setAllOutputFields(clazz)
            .inTx(inTxId)
        val response = update<T>(builder)
        return response.validateStatusAndValueNotNull(classNameShort<T>())
    }

    /**
     * Создание пустой сущности [T].
     * В ответе будет только [io.extremum.groundClient.builder.constant.BuilderConstants.ID_FIELD_NAME].
     */
    suspend inline fun <reified T : BasicModel<*>> createEmpty(inTxId: TxId? = null): T {
        val clazz = T::class
        val builder = Builders.update()
            .setInput(
                clazz.java.constructors.first().newInstance() as BasicModel<*>
            )
            .inTx(inTxId)
        val response = update<T>(builder)
        return response.validateStatusAndValueNotNull(classNameShort<T>())
    }

    /**
     * Получение сущности по id
     */
    suspend inline fun <reified T : BasicModel<*>> getById(builder: GraphQlGetByIdBuilder): Response<T> =
        apiQueryExecutor.execute<T, T>(builder)

    /**
     * Упрощенное получение сущности по id со всеми полями (кроме полей со списками).
     * Если необходимо задать какие-то настройки, нужно использовать [getById] с заполненным [GraphQlGetByIdBuilder].
     */
    suspend inline fun <reified T : BasicModel<*>> getById(id: Any, inTxId: TxId? = null): T? {
        val response = getById<T>(
            Builders.getById(id)
                .setAllOutputFields(T::class)
                .inTx(inTxId)
        )
        return response.validateStatusAndGetValue(classNameShort<T>())
    }

    /**
     * Удаление сущности.
     * Возвращает true при успешном удалении, false - сущность с заданным id не найдена для удаления.
     */
    suspend fun remove(builder: GraphQlRemoveBuilder): Response<Boolean> {
        val response = apiQueryExecutor.execute<Boolean>(builder, "delete")
        return response.copy(value = response.value ?: false)
    }

    /**
     * Упрощенное удаление сущности c id [id].
     * Возвращает true при успешном удалении, false - сущность с заданным id не найдена для удаления.
     * Если необходимо задать какие-то настройки при создании, нужно использовать [remove] с заполненным [GraphQlRemoveBuilder].
     */
    suspend fun remove(id: Any, inTxId: TxId? = null): Boolean {
        val builder = Builders.remove(id)
            .inTx(inTxId)
        val response = remove(builder)
        return response.validateStatusAndValueNotNull()
    }

    /**
     * Добавление в сущность [T] значения в дочерний список (удаление из сущности [T] значения из дочернего списка).
     * [R] - тип сущностей в дочернем списке.
     */
    suspend inline fun <reified T : BasicModel<*>, reified R : BasicModel<*>> updateSublist(builder: GraphQlUpdateSublistBuilder): Response<List<R>> {
        val sublistMutationName = builder.sublistMutationName
            ?: run { throw IllegalArgumentException("sublist name must be set") }
        val (rootResponse, status, txId, action) = apiQueryExecutor.execute<T, Map<String, Any?>>(builder)
        val sublistResponse = rootResponse?.get(sublistMutationName)?.convertValue<QueryResponse>()
        return Response(
            value = sublistResponse?.edges?.mapNotNull { it.node?.convertValue() } ?: listOf(),
            status = status,
            txId = txId,
            action = action,
        )
    }

    /**
     * Начать транзакцию.
     */
    suspend fun beginTx(): Response<Boolean> = apiQueryExecutor.txAction(randomUUID().toString(), Action.BEGIN_TX)

    /**
     * Commit (фиксация) транзакции txId.
     * О последовательности операций над транзакциями см. [WithTxAction].
     * Возвращает true, если произошел commit или нет необходимости фиксировать,
     * то есть если запрос прошел успешно для существующей транзакции.
     */
    suspend fun commit(txId: TxId): Response<Boolean> = apiQueryExecutor.txAction(txId, Action.COMMIT)

    /**
     * Rollback транзакции txId.
     * О последовательности операций над транзакциями см. [WithTxAction].
     * Возвращает true, если произошел откат или нет необходимости откатывать,
     * то есть если запрос прошел успешно для существующей транзакции.
     */
    suspend fun rollback(txId: TxId): Response<Boolean> = apiQueryExecutor.txAction(txId, Action.ROLLBACK)

    /**
     * Выполнение блока действий [block] в транзакции.
     * До блока начинается транзакция, после - выполняется commit/rollback.
     * При ошибке в блоке/работе с транзакциями выполняется блок действий [onError].
     */
    suspend fun <T> tx(block: suspend (txId: TxId) -> T, onError: suspend () -> Unit = {}): T {
        val txId = randomUUID().toString()
        try {
            val result = block(txId)
            commit(txId)
            return result
        } catch (e: Exception) {
            onError()
            rollback(txId)
            throw e
        }
    }

    /**
     * См. [tx]
     */
    suspend fun <T> tx(block: suspend (txId: TxId) -> T): T = tx(block = block, onError = {})

    data class QueryResponse(
        val edges: List<Edge>? = null
    )

    data class Edge(
        val node: Any? = null
    )
}