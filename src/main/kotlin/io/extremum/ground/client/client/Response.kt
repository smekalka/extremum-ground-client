package io.extremum.ground.client.client

import io.extremum.ground.client.builder.core.Action
import io.extremum.ground.client.builder.tx.EntityWithTx
import io.extremum.ground.client.builder.tx.TxId
import io.extremum.ground.client.client.Response.Status.BAD_REQUEST
import io.extremum.ground.client.client.Response.Status.INTERNAL_SERVER_ERROR
import io.extremum.ground.client.client.Response.Status.OK
import io.extremum.ground.client.client.Response.Status.OTHER_ERROR
import io.extremum.ground.client.client.Response.Status.UNAUTHORIZED
import org.springframework.http.HttpStatus

data class Response<T>(
    /**
     * Значение может быть не заполнено в одном из следующих случаев:
     *  - запрос не прошел успешно ([status] неуспешный)
     *  - сущность не найдена по запросу get by id
     */
    val value: T?,
    val status: Status = OK,
    val txId: TxId? = null,
    val action: Action,
) {

    fun validateStatus(entityName: String) {
        val entityMsgPart = if (action.withTx) "" else " ${entityName.lowercase()}"
        status.validate("${action.msg}$entityMsgPart")
    }

    fun validateStatusAndValueNotNull(entityName: String = DEFAULT_ENTITY_NAME): T {
        validateStatus(entityName)
        return validateValueNotNull(entityName)
    }

    fun validateStatusAndGetValue(entityName: String = DEFAULT_ENTITY_NAME): T? {
        validateStatus(entityName)
        return value
    }

    fun validateStatusAndTx(entityName: String = DEFAULT_ENTITY_NAME): EntityWithTx<T?> {
        validateStatus(entityName)
        return EntityWithTx(value, validateTxIdNotNull())
    }

    fun validateStatusValueAndTx(entityName: String = DEFAULT_ENTITY_NAME): EntityWithTx<T> {
        validateStatus(entityName)
        return EntityWithTx(validateValueNotNull(entityName), validateTxIdNotNull())
    }

    private fun validateValueNotNull(entityName: String = DEFAULT_ENTITY_NAME): T {
        if (value == null) {
            if (action == Action.GET_BY_ID) {
                throw IllegalStateException("$entityName not found")
            }
            throw IllegalStateException("Validation error. Value can't be null in response $this")
        }
        return value
    }

    private fun validateTxIdNotNull(): TxId {
        txId ?: throw IllegalStateException("Validation error. TxId can't be null in response $this")
        return txId
    }

    enum class Status(val successful: Boolean = false) {
        OK(true),
        INTERNAL_SERVER_ERROR,
        BAD_REQUEST,
        UNAUTHORIZED,
        OTHER_ERROR,
        MODEL_NOT_FOUND,
        DATA_FETCHING_EXCEPTION,
        INVALID_SYNTAX,
        VALIDATION_ERROR,
        TX_NOT_FOUND,
        ;

        internal fun validate(processName: String) {
            if (!this.successful) {
                throw IllegalStateException("Can't $processName. Status: $this")
            }
        }
    }

    companion object {
        fun HttpStatus.toStatus(): Status = when (this) {
            HttpStatus.OK -> OK
            HttpStatus.INTERNAL_SERVER_ERROR -> INTERNAL_SERVER_ERROR
            HttpStatus.BAD_REQUEST -> BAD_REQUEST
            HttpStatus.UNAUTHORIZED -> UNAUTHORIZED
            else -> OTHER_ERROR
        }
        
        private const val DEFAULT_ENTITY_NAME = "Entity"
    }
}