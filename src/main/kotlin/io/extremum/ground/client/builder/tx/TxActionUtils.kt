package io.extremum.ground.client.builder.tx

import io.extremum.ground.client.builder.tx.TxActionType.BEGIN
import io.extremum.ground.client.builder.tx.TxActionType.COMMIT
import io.extremum.ground.client.builder.tx.TxActionType.IN_TX
import io.extremum.ground.client.builder.tx.TxConstants.TX_HEADER_NAME
import io.extremum.ground.client.builder.tx.TxConstants.TX_ID_COOKIE_NAME
import io.extremum.ground.client.builder.tx.TxConstants.TX_ROLLBACK_HEADER_VALUE
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec

object TxActionUtils {

    fun <S : RequestHeadersSpec<S>> RequestHeadersSpec<S>.applyTxAction(txAction: TxAction?): RequestHeadersSpec<S> =
        this.also { request ->
            if (txAction != null) {
                val (type, id) = txAction
                when (type) {
                    BEGIN -> request.header(TX_HEADER_NAME, type.headerValue)
                        // todo подпорка. проблема в ground: только с кукой JSESSIONID он отдает tx на x-tx=begin
                        .cookie("JSESSIONID", "")

                    IN_TX -> request.cookie(TX_ID_COOKIE_NAME, id!!)
                    COMMIT -> request.header(TX_HEADER_NAME, type.headerValue)
                        .cookie(TX_ID_COOKIE_NAME, id!!)
                }
            }
        }

    fun <S : RequestHeadersSpec<S>> RequestHeadersSpec<S>.applyTxRollbackAction(txId: String): RequestHeadersSpec<S> =
        this.header(TX_HEADER_NAME, TX_ROLLBACK_HEADER_VALUE)
            .cookie(TX_ID_COOKIE_NAME, txId)

    fun getTxIdCookie(response: ClientResponse, txAction: TxAction?): String? =
        txAction?.run { response.cookies().getFirst(TX_ID_COOKIE_NAME)?.value }
}