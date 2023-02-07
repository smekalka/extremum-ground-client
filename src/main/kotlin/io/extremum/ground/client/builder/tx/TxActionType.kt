package io.extremum.ground.client.builder.tx

import io.extremum.ground.client.builder.tx.TxConstants.TX_BEGIN_HEADER_VALUE
import io.extremum.ground.client.builder.tx.TxConstants.TX_COMMIT_HEADER_VALUE

/**
 * См. [WithTxAction]
 */
enum class TxActionType(val headerValue: String? = null) {
    BEGIN(TX_BEGIN_HEADER_VALUE),
    IN_TX,
    COMMIT(TX_COMMIT_HEADER_VALUE),
}
