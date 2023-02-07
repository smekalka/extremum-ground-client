package io.extremum.ground.client.builder.tx

data class EntityWithTx<T> (
    val value: T,
    val txId: TxId,
)