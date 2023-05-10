package io.extremum.ground.client.url

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
internal class ApiUrlHolder(
    @Value("\${extremum.ground.client.uri:}")
    uri: String = "",
    @Value("\${extremum.ground.client.baseUrl}")
    baseUrl: String,
    @Value("\${extremum.ground.client.xAppId}")
    xAppId: String,
    @Value("\${extremum.ground.client.path}")
    path: String,
) {

    val apiUrl: String = getApiUrl(uri, baseUrl, xAppId, path)

    private companion object {
        fun getApiUrl(uri: String, baseUrl: String, xAppId: String, path: String): String =
            uri.ifEmpty {
                baseUrl.replace("://api", "://api.app-$xAppId")
            } + path
    }
}