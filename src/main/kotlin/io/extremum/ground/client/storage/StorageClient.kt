package io.extremum.ground.client.storage

import io.extremum.ground.client.client.HeadersHolder
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asContextElement
import kotlinx.coroutines.future.future
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitExchange
import java.util.concurrent.CompletableFuture
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
    headers: Map<String, String> = mapOf(),
    private val webClient: WebClient = WebClient.create(uri + path),
) {

    private val logger: Logger = Logger.getLogger(this::class.java.name)

    init {
        updateHeaders(headers)
    }

    final fun updateHeaders(headers: Map<String, String>) {
        HeadersHolder.headers.set(headers)
    }

    /**
     * Получение объектов с ограничением количества [limit], начиная с позиции [offset], с фильтрацией по префиксу [prefix].
     */
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
        return RequestExecutor.requestWithPagination(
            webClient.get()
                .uri(uri)
                .addHeaders()
        )
    }

    /**
     * Получение объекта по ключу [key].
     */
    suspend fun getObject(key: String): ByteArray? {
        val (responseBody, statusCode) = webClient.get()
            .uri(OBJECT.fillArgs(key))
            .addHeaders()
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
                                status = statusCode,
                                message = responseBody.alerts.joinToString { it.code + ": " + it.message }
                            )
                        }
                        throw ExtremumApiException(code = responseBody.code, message = "request failed.")
                    } catch (e: Exception) {
                        throw ExtremumApiException(status = statusCode, message = "request failed.")
                    }
                }
            }
        return if (statusCode == HttpStatus.NOT_FOUND) null else responseBody
    }

    /**
     * Сохранение объекта [obj] по ключу [key].
     */
    suspend fun postObject(key: String, obj: ByteArray) {
        RequestExecutor.requestRaw(
            webClient.post()
                .uri(OBJECT.fillArgs(key))
                .bodyValue(obj)
                .addHeaders()
        )
    }

    /**
     * Удаление объекта с ключом [key].
     */
    suspend fun deleteObject(key: String) {
        RequestExecutor.requestRaw(
            webClient.delete()
                .uri(OBJECT.fillArgs(key))
                .addHeaders()
        )
    }

    /**
     * Начать составную загрузку.
     * Возвращается id загрузки.
     * Если объекта с таким [key] не существует, результат будет null.
     */
    suspend fun startMultipartUpload(key: String): String? =
        RequestExecutor.request(
            webClient.post()
                .uri(OBJECT_MULTIPART.fillArgs(key))
                .addHeaders()
        )

    /**
     * Получить статус составной загрузки [upload] объекта с ключом [key].
     */
    suspend fun getStatusMultipartUpload(key: String, upload: String): List<PartStatusMultipartUpload> =
        RequestExecutor.requestList(
            webClient.get()
                .uri(OBJECT_MULTIPART_UPLOAD.fillArgs(key, upload))
                .addHeaders()
        )

    /**
     * Получить активные составные загрузки объекта с ключом [key].
     * Ограничить список количеством [limit], начать с [offset], отфильтровать по префиксу [prefix].
     */
    suspend fun getMultipartUploadsInProgress(
        key: String,
        limit: Int? = null,
        offset: Int? = null,
        prefix: String? = null
    ): Pair<List<UploadWithMetadata>, Pagination> {
        val uri = "${OBJECT_MULTIPART.fillArgs(key)}?" + buildApiParams(
            "limit" to limit,
            "offset" to offset,
            "prefix" to prefix,
        )
        return RequestExecutor.requestWithPagination(
            webClient.get()
                .uri(uri)
                .addHeaders()
        )
    }

    /**
     * Завершить составную загрузку [upload] объекта с ключом [key].
     */
    suspend fun completeMultipartUpload(key: String, upload: String) {
        RequestExecutor.requestRaw(
            webClient.put()
                .uri(OBJECT_MULTIPART_UPLOAD.fillArgs(key, upload))
                .addHeaders()
        )
    }

    /**
     * Отменить составную загрузку [upload] объекта с ключом [key].
     */
    suspend fun abortMultipartUpload(key: String, upload: String) {
        RequestExecutor.requestRaw(
            webClient.delete()
                .uri(OBJECT_MULTIPART_UPLOAD.fillArgs(key, upload))
                .addHeaders()
        )
    }

    /**
     * Загрузить составную часть [obj] с номером [part] в загрузке [upload] объекта с ключом [key].
     */
    suspend fun uploadPart(key: String, upload: String, part: Int, obj: ByteArray): String? =
        RequestExecutor.request(
            webClient.post()
                .uri(OBJECT_MULTIPART_UPLOAD.fillArgs(key, upload) + "?part=" + part)
                .bodyValue(obj)
                .addHeaders()
        )

    /**
     * Получить информацию об объекте с ключом [key].
     */
    suspend fun getObjectMeta(key: String): ObjectMetadata? =
        RequestExecutor.request(
            webClient.get()
                .uri(OBJECT_META.fillArgs(key))
                .addHeaders()
        )

    /**
     * Получить подписанную ссылку для объекта с ключом [key].
     */
    suspend fun getPresignedUrl(key: String, body: GetPresignedUrlBody): String? =
        RequestExecutor.request(
            webClient.post()
                .uri(OBJECT_PRESIGN_URL.fillArgs(key))
                .bodyValue(body)
                .addHeaders()
        )

    suspend fun createPostFormForUpload(key: String): String? =
        RequestExecutor.request(
            webClient.post()
                .uri(OBJECT_UPLOAD_FORM.fillArgs(key))
                .addHeaders()
        )

    /**
     * Получить список операций.
     */
    suspend fun listJobs(): String? =
        RequestExecutor.request(
            webClient.get()
                .uri(JOBS)
                .addHeaders()
        )

    /**
     * Создать операцию.
     */
    suspend fun createJob(): String? =
        RequestExecutor.request(
            webClient.post()
                .uri(JOBS)
                .addHeaders()
        )

    /**
     * Получить информацию об операции [job].
     */
    suspend fun getJobMetaData(job: String): String? =
        RequestExecutor.request(
            webClient.get()
                .uri(JOB.fillArgs(job))
                .addHeaders()
        )

    /**
     * Удалить операцию [job].
     */
    suspend fun deleteJob(job: String): String? =
        RequestExecutor.request(
            webClient.delete()
                .uri(JOB.fillArgs(job))
                .addHeaders()
        )

    /**
     * Аналог [getObjects].
     */
    fun getObjectsF(
        limit: Int? = null,
        offset: Int? = null,
        prefix: String? = null
    ): CompletableFuture<Pair<List<ObjectMetadata>, Pagination>> =
        CoroutineScope(Dispatchers.Default + headersAsContextElement()).future {
            getObjects(limit, offset, prefix)
        }

    /**
     * Аналог [getObject].
     */
    fun getObjectF(key: String): CompletableFuture<ByteArray?> =
        CoroutineScope(Dispatchers.Default + headersAsContextElement()).future {
            getObject(key)
        }

    /**
     * Аналог [postObject].
     */
    fun postObjectF(key: String, obj: ByteArray): CompletableFuture<Unit> =
        CoroutineScope(Dispatchers.Default + headersAsContextElement()).future {
            postObject(key, obj)
        }

    /**
     * Аналог [deleteObject].
     */
    fun deleteObjectF(key: String): CompletableFuture<Unit> =
        CoroutineScope(Dispatchers.Default + headersAsContextElement()).future {
            deleteObject(key)
        }

    /**
     * Аналог [startMultipartUpload].
     */
    fun startMultipartUploadF(key: String): CompletableFuture<String?> =
        CoroutineScope(Dispatchers.Default + headersAsContextElement()).future {
            startMultipartUpload(key)
        }

    /**
     * Аналог [getStatusMultipartUpload].
     */
    fun getStatusMultipartUploadF(key: String, upload: String): CompletableFuture<List<PartStatusMultipartUpload>> =
        CoroutineScope(Dispatchers.Default + headersAsContextElement()).future {
            getStatusMultipartUpload(key, upload)
        }

    /**
     * Аналог [getMultipartUploadsInProgress].
     */
    fun getMultipartUploadsInProgressF(
        key: String,
        limit: Int,
        offset: Int,
        prefix: String = ""
    ): CompletableFuture<Pair<List<UploadWithMetadata>, Pagination>> =
        CoroutineScope(Dispatchers.Default + headersAsContextElement()).future {
            getMultipartUploadsInProgress(
                key,
                limit,
                offset,
                prefix,
            )
        }

    /**
     * Аналог [completeMultipartUpload].
     */
    fun completeMultipartUploadF(key: String, upload: String): CompletableFuture<Unit> =
        CoroutineScope(Dispatchers.Default + headersAsContextElement()).future {
            completeMultipartUpload(key, upload)
        }

    /**
     * Аналог [abortMultipartUpload].
     */
    fun abortMultipartUploadF(key: String, upload: String): CompletableFuture<Unit> =
        CoroutineScope(Dispatchers.Default + headersAsContextElement()).future {
            abortMultipartUpload(key, upload)
        }

    /**
     * Аналог [uploadPart].
     */
    fun uploadPartF(key: String, upload: String, part: Int, obj: ByteArray): CompletableFuture<String?> =
        CoroutineScope(Dispatchers.Default + headersAsContextElement()).future {
            uploadPart(key, upload, part, obj)
        }

    /**
     * Аналог [getObjectMeta].
     */
    fun getObjectMetaF(key: String): CompletableFuture<ObjectMetadata?> =
        CoroutineScope(Dispatchers.Default + headersAsContextElement()).future {
            getObjectMeta(key)
        }

    /**
     * Аналог [getPresignedUrl].
     */
    fun getPresignedUrlF(key: String, body: GetPresignedUrlBody): CompletableFuture<String?> =
        CoroutineScope(Dispatchers.Default + headersAsContextElement()).future {
            getPresignedUrl(key, body)
        }

    /**
     * Аналог [createPostFormForUpload].
     */
    fun createPostFormForUploadF(key: String): CompletableFuture<String?> =
        CoroutineScope(Dispatchers.Default + headersAsContextElement()).future {
            createPostFormForUpload(key)
        }

    /**
     * Аналог [listJobs].
     */
    fun listJobsF(): CompletableFuture<String?> =
        CoroutineScope(Dispatchers.Default + headersAsContextElement()).future {
            listJobs()
        }

    /**
     * Аналог [createJob].
     */
    fun createJobF(): CompletableFuture<String?> =
        CoroutineScope(Dispatchers.Default + headersAsContextElement()).future {
            createJob()
        }

    /**
     * Аналог [getJobMetaData].
     */
    fun getJobMetaDataF(job: String): CompletableFuture<String?> =
        CoroutineScope(Dispatchers.Default + headersAsContextElement()).future {
            getJobMetaData(job)
        }

    /**
     * Аналог [deleteJob].
     */
    fun deleteJobF(job: String): CompletableFuture<String?> =
        CoroutineScope(Dispatchers.Default + headersAsContextElement()).future {
            deleteJob(job)
        }

    private fun <T : WebClient.RequestHeadersSpec<T>> WebClient.RequestHeadersSpec<T>.addHeaders(): WebClient.RequestHeadersSpec<T> =
        apply {
            this.header(X_APP_ID_HEADER, xAppId)
            HeadersHolder.headers.get()?.forEach { (name, value) ->
                this.header(name, value)
            }
        }

    companion object {
        const val X_APP_ID_HEADER = "x-app-id"

        private fun headersAsContextElement() = HeadersHolder.headers.asContextElement(HeadersHolder.headers.get())
    }
}