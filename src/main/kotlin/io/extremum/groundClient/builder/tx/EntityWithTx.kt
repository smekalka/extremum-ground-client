package io.extremum.groundClient.builder.tx

data class EntityWithTx<T> (
    val value: T,
    val txId: TxId,
)