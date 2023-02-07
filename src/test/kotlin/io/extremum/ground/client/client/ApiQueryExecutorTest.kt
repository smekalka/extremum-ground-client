package io.extremum.ground.client.client

import io.extremum.ground.client.builder.Builders
import io.extremum.ground.client.builder.core.Action
import io.extremum.ground.client.builder.tx.TxConstants.TX_ID_COOKIE_NAME
import io.extremum.ground.client.builder.tx.beginTx
import io.extremum.ground.client.builder.tx.inTx
import io.extremum.ground.client.model.Account
import io.extremum.ground.client.model.Change
import io.extremum.ground.client.model.Zone
import io.extremum.ground.client.client.ApiQueryExecutor.Companion.GRAPHQL_URI
import io.extremum.ground.client.client.Response.Status.INTERNAL_SERVER_ERROR
import io.extremum.sharedmodels.basic.StringOrObject
import io.extremum.sharedmodels.descriptor.Descriptor
import io.extremum.test.tools.StringUtils.toDescriptor
import io.extremum.test.tools.StringUtils.toStringOrMultilingual
import io.extremum.test.tools.ToJsonFormatter.toJson
import kotlinx.coroutines.runBlocking
import org.apache.http.HttpHeaders
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class ApiQueryExecutorTest {

    private lateinit var apiQueryExecutor: ApiQueryExecutor

    @Mock(lenient = true)
    private lateinit var webClientMock: WebClient

    @Mock(lenient = true)
    private lateinit var requestBodyUriMock: WebClient.RequestBodyUriSpec

    @BeforeEach
    fun beforeEach() {
        apiQueryExecutor = ApiQueryExecutor(url, mapOf("auth" to authToken), "0", webClientMock)
    }

    @Test
    fun `get zones`() {
        runBlocking {
            val queryResponse = GroundApiClient.QueryResponse(
                listOf(
                    GroundApiClient.Edge(zone(uuid = "1", description = "desc 1")),
                    GroundApiClient.Edge(zone(uuid = "2", description = "desc 2")),
                    GroundApiClient.Edge(zone(uuid = "3", description = "desc 3")),
                )
            )
            val body = "{\"data\": {\"zones\": ${queryResponse.toJson()}}}"
            mockClientResponse(body = body, httpStatus = HttpStatus.OK)

            val builder = Builders.query()
            val result = apiQueryExecutor.execute<_root_ide_package_.io.extremum.ground.client.model.Zone, GroundApiClient.QueryResponse>(builder)

            assertThat(result.value?.toJson()).isEqualTo(queryResponse.toJson())
            assertThat(result.status).isEqualTo(Response.Status.OK)
            verify(requestBodyUriMock).bodyValue(builder.build("zones"))
        }
    }

    private fun zone(uuid: String, description: String): _root_ide_package_.io.extremum.ground.client.model.Zone =
        _root_ide_package_.io.extremum.ground.client.model.Zone().apply {
            this.uuid = uuid.toDescriptor()
            this.description = description.toStringOrMultilingual()
        }

    /**
     * create и update имеют тот же ответ, что и getById: execute<T, T>.
     * Поэтому достаточно проверить getById
     */
    @Test
    fun `get by id`() {
        runBlocking {
            val account = _root_ide_package_.io.extremum.ground.client.model.Account().apply {
                uuid = randomUuidDescriptor()
            }
            val body = "{\"data\": {\"account\": ${account.toJson()}}}"
            mockClientResponse(body = body, httpStatus = HttpStatus.OK)

            val builder = Builders.getById(randomUuidDescriptor())
            val result = apiQueryExecutor.execute<_root_ide_package_.io.extremum.ground.client.model.Account, _root_ide_package_.io.extremum.ground.client.model.Account>(builder)

            assertThat(result).isEqualTo(Response(account, action = Action.GET_BY_ID))
            verify(requestBodyUriMock).bodyValue(builder.build("account"))
        }
    }

    @Test
    fun remove() {
        runBlocking {
            val deleted = true
            val body = "{\"data\": {\"delete\": $deleted}}"
            mockClientResponse(body = body, httpStatus = HttpStatus.OK)

            val builder = Builders.remove(randomUuidDescriptor())
            val result = apiQueryExecutor.execute<Boolean?>(builder, "delete")

            assertThat(result).isEqualTo(Response(deleted, action = Action.REMOVE))
            verify(requestBodyUriMock).bodyValue(builder.build("delete"))
        }
    }

    @Test
    fun `add to sublist`() {
        runBlocking {
            val queryResponse = GroundApiClient.QueryResponse(
                listOf(
                    GroundApiClient.Edge(change(uuid = "1", data = "desc 1")),
                    GroundApiClient.Edge(change(uuid = "2", data = "desc 2")),
                    GroundApiClient.Edge(change(uuid = "3", data = "desc 3")),
                )
            )
            val body = "{\"data\": {\"account\": {\"addChanges\": ${queryResponse.toJson()}}}}"
            mockClientResponse(body = body, httpStatus = HttpStatus.OK)

            val builder = Builders.addToSublist(
                id = randomUuidDescriptor(),
                sublistFieldGetter = _root_ide_package_.io.extremum.ground.client.model.Account::getChanges,
                entityToAdd = change(uuid = "4", data = "desc 4")
            )
            val result = apiQueryExecutor.execute<_root_ide_package_.io.extremum.ground.client.model.Account, Map<String, Any?>>(builder)

            assertThat(result.value?.toJson()).isEqualTo(mapOf("addChanges" to queryResponse).toJson())
            assertThat(result.status).isEqualTo(Response.Status.OK)
            verify(requestBodyUriMock).bodyValue(builder.build("account"))
        }
    }

    private fun change(uuid: String, data: String): _root_ide_package_.io.extremum.ground.client.model.Change =
        _root_ide_package_.io.extremum.ground.client.model.Change().apply {
            this.uuid = uuid.toDescriptor()
            this.data = StringOrObject(data)
        }

    @Test
    fun `remove from sublist`() {
        runBlocking {
            val queryResponse = GroundApiClient.QueryResponse(
                listOf(
                    GroundApiClient.Edge(change(uuid = "1", data = "desc 1")),
                    GroundApiClient.Edge(change(uuid = "2", data = "desc 2")),
                    GroundApiClient.Edge(change(uuid = "3", data = "desc 3")),
                )
            )
            val body = "{\"data\": {\"account\": {\"removeChanges\": ${queryResponse.toJson()}}}}"
            mockClientResponse(body = body, httpStatus = HttpStatus.OK)

            val builder = Builders.removeFromSublist(
                id = randomUuidDescriptor(),
                sublistFieldGetter = _root_ide_package_.io.extremum.ground.client.model.Account::getChanges,
                idToRemove = randomUuidDescriptor()
            )
            val result = apiQueryExecutor.execute<_root_ide_package_.io.extremum.ground.client.model.Account, Map<String, Any?>>(builder)

            assertThat(result.value?.toJson()).isEqualTo(mapOf("removeChanges" to queryResponse).toJson())
            assertThat(result.status).isEqualTo(Response.Status.OK)
            verify(requestBodyUriMock).bodyValue(builder.build("account"))
        }
    }

    @Test
    fun `get by id, in tx`() {
        runBlocking {
            val account = _root_ide_package_.io.extremum.ground.client.model.Account().apply {
                uuid = randomUuidDescriptor()
            }
            val body = "{\"data\": {\"account\": ${account.toJson()}}}"
            val responseTx = "111"
            mockClientResponse(body = body, httpStatus = HttpStatus.OK, txId = responseTx)

            val requestInTx = "222"
            val builder = Builders.getById(randomUuidDescriptor())
                .inTx(requestInTx)
            val result = apiQueryExecutor.execute<_root_ide_package_.io.extremum.ground.client.model.Account, _root_ide_package_.io.extremum.ground.client.model.Account>(builder)

            assertThat(result).isEqualTo(Response(account, txId = responseTx, action = Action.GET_BY_ID))
        }
    }

    @Test
    fun `get by id, begin tx`() {
        runBlocking {
            val account = _root_ide_package_.io.extremum.ground.client.model.Account().apply {
                uuid = randomUuidDescriptor()
            }
            val body = "{\"data\": {\"account\": ${account.toJson()}}}"
            val responseTx = "111"
            mockClientResponse(body = body, httpStatus = HttpStatus.OK, txId = responseTx)

            val builder = Builders.getById(randomUuidDescriptor())
                .beginTx()
            val result = apiQueryExecutor.execute<_root_ide_package_.io.extremum.ground.client.model.Account, _root_ide_package_.io.extremum.ground.client.model.Account>(builder)

            assertThat(result).isEqualTo(Response(account, txId = responseTx, action = Action.GET_BY_ID))
        }
    }

    @Test
    fun `get by id, error`() {
        runBlocking {
            mockClientResponse(body = null, httpStatus = HttpStatus.INTERNAL_SERVER_ERROR)

            val builder = Builders.getById(randomUuidDescriptor())
            val result = apiQueryExecutor.execute<_root_ide_package_.io.extremum.ground.client.model.Account, _root_ide_package_.io.extremum.ground.client.model.Account>(builder)

            assertThat(result).isEqualTo(Response(null, INTERNAL_SERVER_ERROR, action = Action.GET_BY_ID))
        }
    }

    private fun mockClientResponse(body: String?, httpStatus: HttpStatus, txId: String? = null) {
        whenever(webClientMock.post()).thenReturn(requestBodyUriMock)
        whenever(requestBodyUriMock.uri(GRAPHQL_URI)).thenReturn(requestBodyUriMock)
        val clientResponse = ClientResponse.create(httpStatus)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .apply {
                if (txId != null) {
                    cookie(TX_ID_COOKIE_NAME, txId)
                }
                if (body != null) {
                    body(body)
                }
            }
            .build()
        val requestBody = TestRequestBodyUriSpec(clientResponse)
        whenever(requestBodyUriMock.bodyValue(any())).thenReturn(requestBody)
    }

    private companion object {

        fun randomUuidDescriptor(): Descriptor = UUID.randomUUID().toString().toDescriptor()

        const val authToken = "token"
        const val url = "uri"
    }
}