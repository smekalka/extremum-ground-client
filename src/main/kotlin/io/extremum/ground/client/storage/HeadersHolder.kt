package io.extremum.ground.client.storage

object HeadersHolder {

    val headers = ThreadLocal<Map<String, String>?>()
}