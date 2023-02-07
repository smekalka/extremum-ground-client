package io.extremum.groundClient.builder.tx

object TxConstants {

    const val TX_HEADER_NAME = "x-tx"

    const val TX_BEGIN_HEADER_VALUE= "begin"
    const val TX_COMMIT_HEADER_VALUE= "commit"
    const val TX_ROLLBACK_HEADER_VALUE= "rollback"

    const val TX_ID_COOKIE_NAME = "x-txid"
}