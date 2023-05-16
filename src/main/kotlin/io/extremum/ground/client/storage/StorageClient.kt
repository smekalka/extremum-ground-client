package io.extremum.ground.client.storage

import io.extremum.ground.client.storage.Paths.JOB
import io.extremum.ground.client.storage.Paths.JOBS
import io.extremum.ground.client.storage.Paths.OBJECT
import io.extremum.ground.client.storage.Paths.OBJECTS
import io.extremum.ground.client.storage.Paths.OBJECT_META
import io.extremum.ground.client.storage.Paths.OBJECT_MULTIPART
import io.extremum.ground.client.storage.Paths.OBJECT_MULTIPART_UPLOAD
import io.extremum.ground.client.storage.Paths.OBJECT_PRESIGN_URL
import io.extremum.ground.client.storage.Paths.OBJECT_UPLOAD_FORM
import io.extremum.ground.client.storage.requestbody.GetPresignedUrlBody
import io.extremum.model.tools.api.ExtremumApiException
import io.extremum.model.tools.api.RequestExecutor
import io.extremum.model.tools.api.StringUtils.buildApiParams
import io.extremum.model.tools.api.StringUtils.fillArgs
import io.extremum.model.tools.mapper.MapperUtils.convertValue
import io.extremum.sharedmodels.dto.ObjectMetadata
import io.extremum.sharedmodels.dto.Pagination
import io.extremum.sharedmodels.dto.PartStatusMultipartUpload
import io.extremum.sharedmodels.dto.Response
import io.extremum.sharedmodels.dto.UploadWithMetadata
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitExchange
import java.util.logging.Logger

@Component
class StorageClient(
    @Value("\${extremum.ground.client.storage.uri}")
    private val uri: String,
    @Value("\${extremum.ground.client.storage.path}")
    private val path: String,
    @Value("\${extremum.ground.client.xAppId}")
    private val xAppId: String,
    @Value("#{\${extremum.ground.client.storage.headers:{}}}")
    private val headers: Map<String, String> = mapOf(),
    private val webClient: WebClient = WebClient.create(uri + path),
) {
    private val requestExecutor = RequestExecutor(xAppId = xAppId, headers = headers)

    private val logger: Logger = Logger.getLogger(this::class.java.name)

    fun updateHeaders(headers: Map<String, String>) {
        requestExecutor.updateHeaders(headers)
    }

    suspend fun getObjects(
        limit: Int? = null,
        offset: Int? = null,
        prefix: String? = null
    ): Pair<List<ObjectMetadata>, Pagination> {
        val uri = "$OBJECTS?" + buildApiParams(
            "limit" to limit,
            "offset" to offset,
            "prefix" to prefix,
        )
        return requestExecutor.requestWithPagination(
            webClient.get()
                .uri(uri)
        )
    }

    suspend fun getObject(key: String): ByteArray? {
        val (responseBody, statusCode) = webClient.get()
            .uri(OBJECT.fillArgs(key))
            .apply {
                this.header(RequestExecutor.X_APP_ID_HEADER, xAppId)
                headers.forEach { (name, value) ->
                    this.header(name, value)
                }
            }
            .awaitExchange { response ->
                val statusCode = response.statusCode()
                logger.info("Response code: $statusCode")
                val body = response.awaitBody<ByteArray>()
                if (statusCode in RequestExecutor.NOT_FAILED_STATUSES) {
                    body to statusCode
                } else {
                    try {
                        val responseBody = body.convertValue<Response>()
                        if (responseBody.alerts.isNotEmpty()) {
                            logger.warning("Alerts from response body: ${responseBody.alerts}")
                            throw ExtremumApiException(
                                code = statusCode,
                                message = responseBody.alerts.joinToString { it.code + ": " + it.message }
                            )
                        }
                        throw ExtremumApiException(code = responseBody.code, message = "request failed.")
                    } catch (e: Exception) {
                        throw ExtremumApiException(code = statusCode, message = "request failed.")
                    }
                }
            }
        return if (statusCode == HttpStatus.NOT_FOUND) null else responseBody
    }

    suspend fun postObject(key: String, obj: ByteArray) {
        requestExecutor.requestRaw(
            webClient.post()
                .uri(OBJECT.fillArgs(key))
                .bodyValue(obj)
        )
    }

    suspend fun deleteObject(key: String) {
        requestExecutor.requestRaw(
            webClient.delete()
                .uri(OBJECT.fillArgs(key))
        )
    }

    /**
     * Если объекта с таким [key] не существует, результат будет null.
     */
    suspend fun startMultipartUpload(key: String): String? =
        requestExecutor.request(
            webClient.post()
                .uri(OBJECT_MULTIPART.fillArgs(key))
        )

    suspend fun getStatusMultipartUpload(key: String, upload: String): List<PartStatusMultipartUpload> =
        requestExecutor.requestList(
            webClient.get()
                .uri(OBJECT_MULTIPART_UPLOAD.fillArgs(key, upload))
        )

    suspend fun getMultipartUploadsInProgress(
        key: String,
        limit: Int,
        offset: Int,
        prefix: String = ""
    ): Pair<List<UploadWithMetadata>, Pagination> =
        requestExecutor.requestWithPagination(
            webClient.get()
                .uri(OBJECT_MULTIPART.fillArgs(key))
        )

    suspend fun completeMultipartUpload(key: String, upload: String) {
        requestExecutor.requestRaw(
            webClient.put()
                .uri(OBJECT_MULTIPART_UPLOAD.fillArgs(key, upload))
        )
    }

    suspend fun abortMultipartUpload(key: String, upload: String) {
        requestExecutor.requestRaw(
            webClient.delete()
                .uri(OBJECT_MULTIPART_UPLOAD.fillArgs(key, upload))
        )
    }

    suspend fun uploadPart(key: String, upload: String, part: Int, obj: ByteArray): String? =
        requestExecutor.request(
            webClient.post()
                .uri(OBJECT_MULTIPART_UPLOAD.fillArgs(key, upload) + "?part=" + part)
                .bodyValue(obj)
        )

    suspend fun getObjectMeta(key: String): ObjectMetadata? =
        requestExecutor.request(
            webClient.get()
                .uri(OBJECT_META.fillArgs(key))
        )

    suspend fun getPresignedUrl(key: String, body: GetPresignedUrlBody): String? =
        requestExecutor.request(
            webClient.post()
                .uri(OBJECT_PRESIGN_URL.fillArgs(key))
                .bodyValue(body)
        )

    suspend fun createPostFormForUpload(key: String): String? =
        requestExecutor.request(
            webClient.post()
                .uri(OBJECT_UPLOAD_FORM.fillArgs(key))
        )


    suspend fun listJobs(): String? =
        requestExecutor.request(
            webClient.get()
                .uri(JOBS)
        )

    suspend fun createJob(): String? =
        requestExecutor.request(
            webClient.post()
                .uri(JOBS)
        )

    suspend fun getJobMetaData(job: String): String? =
        requestExecutor.request(
            webClient.get()
                .uri(JOB.fillArgs(job))
        )

    suspend fun deleteJob(job: String): String? =
        requestExecutor.request(
            webClient.delete()
                .uri(JOB.fillArgs(job))
        )
}