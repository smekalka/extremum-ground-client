package io.extremum.ground.client.client

import io.extremum.ground.client.url.ApiUrlHolder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ApiClientHolder {
    @Autowired
    private lateinit var apiUrlHolder: ApiUrlHolder

    val groundApiClient: GroundApiClient
            by lazy { GroundApiClient(apiUrlHolder.apiUrl) }

    fun updateHeaders(headers: Map<String, String>) {
        groundApiClient.updateHeaders(headers)
    }
}