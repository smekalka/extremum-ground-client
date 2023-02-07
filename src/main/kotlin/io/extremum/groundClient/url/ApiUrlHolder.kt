package io.extremum.groundClient.url

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
internal class ApiUrlHolder(
    @Value("\${apiBaseUrl}")
    baseUrl: String,
    @Value("\${xAppId}")
    xAppId: String,
    @Value("\${groundUri}")
    uri: String,
) {

    val apiUrl: String = getApiUrl(baseUrl, xAppId, uri)

    private companion object {
        fun getApiUrl(baseUrl: String, xAppId: String, uri: String): String = baseUrl.replace("://api", "://api.app-$xAppId") + uri
    }
}