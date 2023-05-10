package io.extremum.ground.client.client

import io.extremum.ground.client.url.ApiUrlHolder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class ApiClientHolder(
    @Value("\${extremum.ground.client.xAppId}")
    private val xAppId: String,
    @Value("\${extremum.ground.client.graphql.path}")
    val graphqlPath: String,
    @Value("\${extremum.ground.client.tx.path}")
    private val txPath: String,
) {
    @Autowired
    private lateinit var apiUrlHolder: ApiUrlHolder

    val groundApiClient: GroundApiClient
            by lazy {
                GroundApiClient(
                    url = apiUrlHolder.apiUrl,
                    xAppId = xAppId,
                    graphqlPath = graphqlPath,
                    txPath = txPath,
                )
            }

    fun updateHeaders(headers: Map<String, String>) {
        groundApiClient.updateHeaders(headers)
    }
}