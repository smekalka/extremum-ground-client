package io.extremum.ground.client.client

import io.extremum.ground.client.builder.core.Action
import io.extremum.ground.client.builder.core.GraphQlBuilder
import io.extremum.ground.client.builder.tx.TxActionUtils.applyTxAction
import io.extremum.ground.client.builder.tx.TxActionUtils.getTxIdCookie
import io.extremum.ground.client.builder.tx.TxId
import io.extremum.ground.client.builder.util.StringUtils.classNameShort
import io.extremum.ground.client.builder.util.StringUtils.unescape
import io.extremum.ground.client.client.Response.Companion.toStatus
import io.extremum.ground.client.client.Response.Status.DATA_FETCHING_EXCEPTION
import io.extremum.ground.client.client.Response.Status.INVALID_SYNTAX
import io.extremum.ground.client.client.Response.Status.MODEL_NOT_FOUND
import io.extremum.ground.client.client.Response.Status.OTHER_ERROR
import io.extremum.ground.client.client.Response.Status.TX_NOT_FOUND
import io.extremum.ground.client.client.Response.Status.VALIDATION_ERROR
import io.extremum.model.tools.mapper.MapperUtils.convertToMap
import io.extremum.model.tools.mapper.MapperUtils.readValue
import io.extremum.sharedmodels.basic.BasicModel
import io.smallrye.graphql.client.GraphQLError
import io.smallrye.graphql.client.impl.ResponseReader
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitExchange
import java.util.logging.Logger
import javax.json.JsonString

class ApiQueryExecutor internal constructor(
    url: String,
    private val xAppId: String,
    val graphqlPath: String,
    private val txPath: String,
    val webClient: WebClient = WebClient.create(url),
) {

    val logger: Logger = Logger.getLogger(this::class.java.name)

    /**
     * Запрос по сущности [T] с ожидаемым ответом [R]
     */
    suspend inline fun <reified T : BasicModel<*>, reified R> execute(builder: GraphQlBuilder): Response<R> =
        execute(T::class.java, R::class.java, builder)

    suspend fun <T : BasicModel<*>, R> execute(
        modelClazz: Class<T>,
        responseClazz: Class<R>,
        builder: GraphQlBuilder
    ): Response<R> {
        val rootName = builder.getRootName(getCollectionName(modelClazz))
        return execute(responseClazz, builder, rootName)
    }

    suspend inline fun <reified R> execute(builder: GraphQlBuilder, rootName: String): Response<R> =
        execute(R::class.java, builder, rootName)

    suspend inline fun <R> execute(clazz: Class<R>, builder: GraphQlBuilder, rootName: String): Response<R> {
        val txAction = builder.txAction
        val requestBody = builder.build(rootName)
        logger.info("\n\nrequestBody:\n${requestBody.unescape()}\ntxAction: $txAction\n")
        val request = webClient.post()
            .uri(graphqlPath)
            .bodyValue(requestBody)
            .addHeaders()
            .applyTxAction(txAction)

        return request
            .awaitExchange { response ->
                val statusCode = response.statusCode()
                logger.info("Response code: $statusCode")
                var status = statusCode.toStatus()
                if (status == OTHER_ERROR) {
                    logger.warning("Unsupported status: $statusCode for request $requestBody")
                }

                val value = if (status.successful) {
                    val body = ResponseReader.readFrom(
                        response.awaitBody(),
                        emptyMap()
                    )
                    logger.info("\n\nresponseBody:\n${body.data}\n")
                    if (body.errors?.isNotEmpty() == true) {
                        val error = body.errors?.first()
                        status = error.toStatus()
                        logger.warning("\n\nError in response: \n${error?.message}\n")
                        null
                    } else {
                        val rootData = body.data?.get(rootName)
                        rootData.toString().readValue(clazz)
                    }
                } else {
                    null
                }

                val txId = getTxIdCookie(response, txAction)

                Response(value = value, status = status, txId = txId, action = builder.action)
            }
    }

    suspend fun txAction(txId: TxId, action: Action): Response<Boolean> =
        try {
            val txActionResponse = webClient
                .let { if (action == Action.BEGIN_TX) it.get() else it.post() }
                .uri(txPath + "/" + action.msg + "/" + txId)
                .addHeaders()
                .retrieve()
                .awaitBody<io.extremum.sharedmodels.dto.Response>()
            logger.info("\n\nFor $action txId: '$txId' txActionResponse:\n$txActionResponse\n")
            val code = txActionResponse.code
            val success = code == HttpStatus.OK.value()
            val httpStatus = try {
                HttpStatus.valueOf(code)
            } catch (e: Exception) {
                logger.warning("Unsupported http code: $code")
                HttpStatus.INTERNAL_SERVER_ERROR
            }
            Response(success, action = action, status = httpStatus.toStatus(), txId = txId)
        } catch (e: WebClientResponseException.BadRequest) {
            logger.info("\n\nFor $action txId: '$txId' tx not found")
            // это Transaction with id ... was not found
            Response(false, action = action, status = TX_NOT_FOUND, txId = txId)
        }

    private fun <T : BasicModel<*>> getCollectionName(clazz: Class<T>): String = classNameShort(clazz).lowercase()

    fun <T : RequestHeadersSpec<T>> RequestHeadersSpec<T>.addHeaders(): RequestHeadersSpec<T> =
        apply {
            this.header(X_APP_ID_HEADER, xAppId)
            HeadersHolder.headers.get()?.forEach { (name, value) ->
                this.header(name, value)
            }
        }

    fun GraphQLError?.toStatus(): Response.Status {
        this ?: return OTHER_ERROR
        val alerts = extensions[ERROR_EXTENSION_ALERTS]
        if (alerts != null) {
            return getStatusFromAlerts(alerts)
        }

        val classification = extensions[ERROR_EXTENSION_CLASSIFICATION]
        if (classification != null) {
            return getStatusFromErrorClassification(classification)
        }

        logger.warning("There is no alerts and classification in response error: $this")
        return OTHER_ERROR
    }

    private fun getStatusFromAlerts(alerts: Any?): Response.Status {
        val firstOrNull = (alerts as List<*>).firstOrNull() ?: return OTHER_ERROR
        val convertToMap = firstOrNull.convertToMap()
        val codeRaw = convertToMap["code"]
        if (codeRaw is JsonString) {
            return when (val code = codeRaw.string) {
                "not-found" -> MODEL_NOT_FOUND
                else -> {
                    logger.warning("Unsupported alert code: $code")
                    OTHER_ERROR
                }
            }
        }
        logger.warning("Unsupported format of code: $codeRaw")
        return OTHER_ERROR
    }

    private fun getStatusFromErrorClassification(classification: Any): Response.Status {
        return when (classification) {
            "DataFetchingException" -> DATA_FETCHING_EXCEPTION
            "InvalidSyntax" -> INVALID_SYNTAX
            "ValidationError" -> VALIDATION_ERROR
            else -> {
                logger.warning("Unsupported error classification: $classification")
                OTHER_ERROR
            }
        }
    }

    companion object {
        private const val ERROR_EXTENSION_CLASSIFICATION = "classification"
        private const val ERROR_EXTENSION_ALERTS = "alerts"
        const val X_APP_ID_HEADER = "x-app-id"
    }
}