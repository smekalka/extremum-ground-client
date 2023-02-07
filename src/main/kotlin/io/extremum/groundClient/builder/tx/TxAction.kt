package io.extremum.groundClient.builder.tx

import io.extremum.groundClient.builder.tx.TxActionType.BEGIN

data class TxAction(
    val type: TxActionType,
    val id: TxId? = null,
) {
    init {
        require(type == BEGIN && id == null || id != null) {
            "tx id must be set for all tx action types except $BEGIN"
        }
    }
}