package io.extremum.ground.client.storage

import io.extremum.ground.client.storage.requestbody.GetPresignedUrlBody
import io.extremum.ground.client.storage.responseresult.ObjectMeta
import io.extremum.ground.client.storage.responseresult.StatusMultipartUpload
import io.extremum.ground.client.storage.responseresult.UploadWithMetadata
import io.extremum.model.tools.api.ExtremumApiException
import io.extremum.model.tools.api.RequestExecutor
import io.extremum.model.tools.mapper.MapperUtils.convertValue
import io.extremum.sharedmodels.dto.Pagination
import io.extremum.sharedmodels.dto.Response
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
        limit: Int,
        offset: Int,
        prefix: String = ""
    ): Pair<List<ObjectMeta>?, Pagination?> =
        requestExecutor.requestWithPagination(
            webClient.get()
                .uri(Paths.OBJECTS)
        )

    suspend fun getObject(key: String): ByteArray? {
        val (responseBody, statusCode) = webClient.get()
            .uri(Paths.OBJECT.fillArgs(key))
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
                .uri(Paths.OBJECT.fillArgs(key))
                .bodyValue(obj)
        )
    }

    suspend fun deleteObject(key: String) {
        requestExecutor.requestRaw(
            webClient.delete()
                .uri(Paths.OBJECT.fillArgs(key))
        )
    }

    suspend fun startMultipartUpload(key: String): String? =
        requestExecutor.request(
            webClient.post()
                .uri(Paths.OBJECT_MULTIPART.fillArgs(key))
        )

    /**
     * Если объекта с таким [key] не существует, результат будет null.
     */
    suspend fun getStatusMultipartUpload(key: String, upload: String): List<StatusMultipartUpload>? =
        requestExecutor.request(
            webClient.get()
                .uri(Paths.OBJECT_MULTIPART_UPLOAD.fillArgs(key, upload))
        )

    suspend fun getMultipartUploadsInProgress(
        key: String,
        limit: Int,
        offset: Int,
        prefix: String = ""
    ): Pair<List<UploadWithMetadata>?, Pagination?> =
        requestExecutor.requestWithPagination(
            webClient.get()
                .uri(Paths.OBJECT_MULTIPART.fillArgs(key))
        )

    suspend fun completeMultipartUpload(key: String, upload: String) {
        requestExecutor.requestRaw(
            webClient.put()
                .uri(Paths.OBJECT_MULTIPART_UPLOAD.fillArgs(key, upload))
        )
    }

    suspend fun abortMultipartUpload(key: String, upload: String) {
        requestExecutor.requestRaw(
            webClient.delete()
                .uri(Paths.OBJECT_MULTIPART_UPLOAD.fillArgs(key, upload))
        )
    }

    suspend fun uploadPart(key: String, upload: String, part: Int, obj: ByteArray): String? =
        requestExecutor.request(
            webClient.post()
                .uri(Paths.OBJECT_MULTIPART_UPLOAD.fillArgs(key, upload) + "?part=" + part)
                .bodyValue(obj)
        )

    suspend fun getObjectMeta(key: String): ObjectMeta? =
        requestExecutor.request(
            webClient.get()
                .uri(Paths.OBJECT_META.fillArgs(key))
        )

    suspend fun getPresignedUrl(key: String, body: GetPresignedUrlBody): String? =
        requestExecutor.request(
            webClient.post()
                .uri(Paths.OBJECT_PRESIGN_URL.fillArgs(key))
                .bodyValue(body)
        )

    suspend fun createPostFormForUpload(key: String): String? =
        requestExecutor.request(
            webClient.post()
                .uri(Paths.OBJECT_UPLOAD_FORM.fillArgs(key))
        )


    suspend fun listJobs(): String? =
        requestExecutor.request(
            webClient.get()
                .uri(Paths.JOBS)
        )

    suspend fun createJob(): String? =
        requestExecutor.request(
            webClient.post()
                .uri(Paths.JOBS)
        )

    suspend fun getJobMetaData(job: String): String? =
        requestExecutor.request(
            webClient.get()
                .uri(Paths.JOB.fillArgs(job))
        )

    suspend fun deleteJob(job: String): String? =
        requestExecutor.request(
            webClient.delete()
                .uri(Paths.JOB.fillArgs(job))
        )

    private fun String.fillArgs(vararg args: Any?): String = String.format(this, *args)
}