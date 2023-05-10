package io.extremum.ground.client.client

import io.extremum.ground.client.builder.Builders
import io.extremum.ground.client.builder.Builders.remove
import io.extremum.ground.client.builder.core.Action
import io.extremum.ground.client.builder.core.GraphQlUpdateSublistBuilder
import io.extremum.ground.client.builder.core.PagingAndSortingRequest
import io.extremum.ground.client.builder.core.setAllOutputFields
import io.extremum.ground.client.builder.impl.GraphQlGetByIdBuilder
import io.extremum.ground.client.builder.impl.GraphQlQueryBuilder
import io.extremum.ground.client.builder.impl.GraphQlRemoveBuilder
import io.extremum.ground.client.builder.impl.GraphQlUpdateBuilder
import io.extremum.ground.client.builder.tx.TxId
import io.extremum.ground.client.builder.tx.WithTxAction
import io.extremum.ground.client.builder.tx.inTx
import io.extremum.ground.client.builder.util.StringUtils.classNameShort
import io.extremum.model.tools.mapper.MapperUtils.convertValue
import io.extremum.sharedmodels.basic.BasicModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import java.util.UUID.randomUUID
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.EmptyCoroutineContext

class GroundApiClient(
    url: String,
    headers: Map<String, String> = mapOf(),
    xAppId: String,
    graphqlPath: String,
    txPath: String,
) {

    val apiQueryExecutor: ApiQueryExecutor

    init {
        apiQueryExecutor = ApiQueryExecutor(
            url = url,
            headers = headers,
            xAppId = xAppId,
            graphqlPath = graphqlPath,
            txPath = txPath,
        )
    }

    fun updateHeaders(headers: Map<String, String>) {
        apiQueryExecutor.updateHeaders(headers)
    }

    /**
     * Запрос по сущностям определенного типа.
     */
    suspend inline fun <reified T : BasicModel<*>> query(builder: GraphQlQueryBuilder): Response<List<T>> =
        queryInner(T::class.java, builder)

    /**
     * Аналог [query].
     */
    fun <T : BasicModel<*>> query(
        clazz: Class<T>,
        builder: GraphQlQueryBuilder
    ): CompletableFuture<Response<List<T>>> =
        CoroutineScope(EmptyCoroutineContext).future {
            queryInner(clazz, builder)
        }

    suspend fun <T : BasicModel<*>> queryInner(clazz: Class<T>, builder: GraphQlQueryBuilder): Response<List<T>> {
        val (response, status, txId, action) = apiQueryExecutor.execute(clazz, QueryResponse::class.java, builder)
        val responseValue = if (response == null) {
            // это случай, когда задали несуществующую транзакцию в inTx
            listOf()
        } else {
            response.edges?.mapNotNull { it.node?.convertValue(clazz) } ?: listOf()
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
    ): List<T> = queryInner(T::class.java, paging, filter, inTxId)

    /**
     * Аналог [query].
     */
    fun <T : BasicModel<*>> query(
        clazz: Class<T>,
        paging: PagingAndSortingRequest = PagingAndSortingRequest(),
        filter: String? = null,
        inTxId: TxId? = null
    ): CompletableFuture<List<T>> = CoroutineScope(EmptyCoroutineContext).future {
        queryInner(clazz, paging, filter, inTxId)
    }

    suspend fun <T : BasicModel<*>> queryInner(
        clazz: Class<T>,
        paging: PagingAndSortingRequest = PagingAndSortingRequest(),
        filter: String? = null,
        inTxId: TxId? = null
    ): List<T> {
        val response = queryInner(
            clazz,
            Builders.query(paging, filter)
                .setAllOutputFields(clazz)
                .inTx(inTxId)
        )
        return response.validateStatusAndValueNotNull(classNameShort(clazz))
    }

    /**
     * Создание/изменение сущности.
     */
    suspend inline fun <reified T : BasicModel<*>> update(builder: GraphQlUpdateBuilder): Response<T> =
        apiQueryExecutor.execute<T, T>(builder)

    /**
     * Аналог [update].
     */
    fun <T : BasicModel<*>> update(
        clazz: Class<T>,
        builder: GraphQlUpdateBuilder
    ): CompletableFuture<Response<T>> = CoroutineScope(EmptyCoroutineContext).future {
        updateInner(clazz, builder)
    }

    private suspend fun <T : BasicModel<*>> updateInner(
        clazz: Class<T>,
        builder: GraphQlUpdateBuilder
    ): Response<T> = apiQueryExecutor.execute(clazz, clazz, builder)

    /**
     * Упрощенное создание сущности [value].
     * В output fields включаются все поля сущности.
     * Если необходимо задать какие-то настройки при создании, нужно использовать [update] с заполненным [GraphQlUpdateBuilder].
     */
    suspend inline fun <reified T : BasicModel<*>> create(value: T, inTxId: TxId? = null): T =
        createInner(T::class.java, value, inTxId)

    /**
     * Аналог [create].
     */
    fun <T : BasicModel<*>> create(clazz: Class<T>, value: T, inTxId: TxId? = null): CompletableFuture<T> =
        CoroutineScope(EmptyCoroutineContext).future {
            createInner(clazz, value, inTxId)
        }

    suspend fun <T : BasicModel<*>> createInner(
        clazz: Class<T>,
        value: T,
        inTxId: TxId?
    ): T {
        val builder = Builders.update()
            .setInput(value)
            .setAllOutputFields(clazz)
            .inTx(inTxId)
        val response = updateInner(clazz, builder)
        return response.validateStatusAndValueNotNull(classNameShort(clazz))
    }

    /**
     * Создание пустой сущности [T].
     * В ответе будет только [io.extremum.ground.client.builder.constant.BuilderConstants.ID_FIELD_NAME].
     */
    suspend inline fun <reified T : BasicModel<*>> createEmpty(inTxId: TxId? = null): T =
        createEmptyInner(T::class.java, inTxId)

    fun <T : BasicModel<*>> createEmpty(clazz: Class<T>, inTxId: TxId? = null): CompletableFuture<T> =
        CoroutineScope(EmptyCoroutineContext).future {
            createEmptyInner(clazz, inTxId)
        }

    suspend fun <T : BasicModel<*>> createEmptyInner(
        clazz: Class<T>,
        inTxId: TxId?
    ): T {
        val builder = Builders.update()
            .setInput(
                clazz.constructors.first().newInstance() as BasicModel<*>
            )
            .inTx(inTxId)
        val response = updateInner(clazz, builder)
        return response.validateStatusAndValueNotNull(classNameShort(clazz))
    }

    /**
     * Получение сущности по id
     */
    suspend inline fun <reified T : BasicModel<*>> getById(builder: GraphQlGetByIdBuilder): Response<T> =
        getByIdInner(T::class.java, builder)

    /**
     * Аналог [getById].
     */
    fun <T : BasicModel<*>> getById(
        clazz: Class<T>,
        builder: GraphQlGetByIdBuilder
    ): CompletableFuture<Response<T>> =
        CoroutineScope(EmptyCoroutineContext).future {
            getByIdInner(clazz, builder)
        }

    suspend fun <T : BasicModel<*>> getByIdInner(
        clazz: Class<T>,
        builder: GraphQlGetByIdBuilder
    ) = apiQueryExecutor.execute(clazz, clazz, builder)

    /**
     * Упрощенное получение сущности по id со всеми полями (кроме полей со списками).
     * Если необходимо задать какие-то настройки, нужно использовать [getById] с заполненным [GraphQlGetByIdBuilder].
     */
    suspend inline fun <reified T : BasicModel<*>> getById(id: Any, inTxId: TxId? = null): T? =
        getByIdInner(T::class.java, id, inTxId)

    /**
     * Аналог [getById].
     */
    fun <T : BasicModel<*>> getById(clazz: Class<T>, id: Any, inTxId: TxId? = null): CompletableFuture<T?> =
        CoroutineScope(EmptyCoroutineContext).future {
            getByIdInner(clazz, id, inTxId)
        }

    suspend fun <T : BasicModel<*>> getByIdInner(
        clazz: Class<T>,
        id: Any,
        inTxId: TxId?
    ): T? {
        val response = getByIdInner(
            clazz,
            Builders.getById(id)
                .setAllOutputFields(clazz)
                .inTx(inTxId)
        )
        return response.validateStatusAndGetValue(classNameShort(clazz))
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
    suspend inline fun <reified T : BasicModel<*>, reified R : BasicModel<*>> updateSublist(builder: GraphQlUpdateSublistBuilder): Response<List<R>> =
        updateSublistInner(T::class.java, R::class.java, builder)

    /**
     * Аналог [updateSublist].
     */
    fun <T : BasicModel<*>, R : BasicModel<*>> updateSublist(
        clazz: Class<T>,
        sublistClazz: Class<R>,
        builder: GraphQlUpdateSublistBuilder
    ): CompletableFuture<Response<List<R>>> = CoroutineScope(EmptyCoroutineContext).future {
        updateSublistInner(clazz, sublistClazz, builder)
    }

    suspend fun <R : BasicModel<*>, T : BasicModel<*>> updateSublistInner(
        clazz: Class<T>,
        sublistClazz: Class<R>,
        builder: GraphQlUpdateSublistBuilder
    ): Response<List<R>> {
        val sublistMutationName = builder.sublistMutationName
            ?: run { throw IllegalArgumentException("sublist name must be set") }
        val (rootResponse, status, txId, action) = apiQueryExecutor.execute(clazz, Map::class.java, builder)
        val sublistResponse = rootResponse?.get(sublistMutationName)?.convertValue<QueryResponse>()
        return Response(
            value = sublistResponse?.edges?.mapNotNull { it.node?.convertValue(sublistClazz) } ?: listOf(),
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
     * Аналог [beginTx].
     */
    fun beginTxF(): CompletableFuture<Response<Boolean>> = CoroutineScope(EmptyCoroutineContext).future {
        beginTx()
    }

    /**
     * Commit (фиксация) транзакции txId.
     * О последовательности операций над транзакциями см. [WithTxAction].
     * Возвращает true, если произошел commit или нет необходимости фиксировать,
     * то есть если запрос прошел успешно для существующей транзакции.
     */
    suspend fun commit(txId: TxId): Response<Boolean> = apiQueryExecutor.txAction(txId, Action.COMMIT)

    /**
     * Аналог [commit].
     */
    fun commitF(txId: TxId): CompletableFuture<Response<Boolean>> =
        CoroutineScope(EmptyCoroutineContext).future {
            commit(txId)
        }

    /**
     * Rollback транзакции txId.
     * О последовательности операций над транзакциями Аналог [WithTxAction].
     * Возвращает true, если произошел откат или нет необходимости откатывать,
     * то есть если запрос прошел успешно для существующей транзакции.
     */
    suspend fun rollback(txId: TxId): Response<Boolean> = apiQueryExecutor.txAction(txId, Action.ROLLBACK)

    /**
     * Аналог [rollback].
     */
    fun rollbackF(txId: TxId): CompletableFuture<Response<Boolean>> = CoroutineScope(EmptyCoroutineContext).future {
        rollback(txId)
    }

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