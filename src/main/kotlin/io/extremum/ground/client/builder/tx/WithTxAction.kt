package io.extremum.ground.client.builder.tx

import io.extremum.ground.client.client.GroundApiClient

/**
 * Последовательность действий при работе с транзакциями:
 * 1. начало транзакции [beginTx]. Может быть выполнено сразу с нужным действием в этой транзакции.
 * 2. действия внутри транзакции с указанием id транзакции [inTx], полученной на предыдущем шаге.
 * Действий могут быть несколько или не быть совсем.
 * 3. commit фиксация выполненных действий с выполняемым действием: [commit] или без - [GroundApiClient.commit]
 * или rollback [GroundApiClient.rollback] для отката выполненных действий
 */
interface WithTxAction {
    var txAction: TxAction?
}

fun <T : WithTxAction> T.beginTx(): T {
    txAction = TxAction(TxActionType.BEGIN)
    return this
}

fun <T : WithTxAction> T.inTx(id: TxId?): T {
    if (id != null) {
        txAction = TxAction(type = TxActionType.IN_TX, id = id)
    }
    return this
}

fun <T : WithTxAction> T.commit(id: TxId): T {
    txAction = TxAction(type = TxActionType.COMMIT, id = id)
    return this
}