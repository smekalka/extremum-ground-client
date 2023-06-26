package io.extremum.ground.client.client

object HeadersHolder {

    val headers = ThreadLocal<Map<String, String>?>()
}