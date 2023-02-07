package io.extremum.groundClient.builder.core

enum class Action(val msg: String, val withTx: Boolean = false) {
    GET_BY_ID("get by id"),
    UPDATE("create or update"),
    QUERY("query"),
    UPDATE_SUBLIST("update sublist of"),
    REMOVE("remove"),

    BEGIN_TX("begin", withTx = true),
    COMMIT("commit", withTx = true),
    ROLLBACK("rollback", withTx = true),
}