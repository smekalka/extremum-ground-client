package io.extremum.ground.client.storage

import io.extremum.ground.client.storage.requestbody.GetPresignedUrlBody.Access.READ
import io.extremum.ground.client.storage.StorageProperties.PATH
import io.extremum.ground.client.storage.StorageProperties.URI
import io.extremum.ground.client.storage.StorageProperties.TOKEN
import io.extremum.ground.client.storage.StorageProperties.X_APP_ID
import io.extremum.ground.client.storage.requestbody.GetPresignedUrlBody
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Disabled
import java.nio.charset.Charset

class StorageClientIT {

    private val client = StorageClient(
        uri = URI,
        path = PATH,
        xAppId = X_APP_ID,
        headers = mapOf(AUTHORIZATION to TOKEN),
    )

    @AfterEach
    fun after() {
        runBlocking {
            client.deleteObject(
                key = KEY,
            )
        }
    }

    @Disabled("launched storage service is needed")
    @Test
    fun getObjects() {
        runBlocking {
            // todo не готово на серверной части
            createObject()
            val (result, pagination) = client.getObjects(
                limit = 10,
                offset = 1,
            )
            println(result)
            // если не найдено
            assertThat(result).isNull()
            assertThat(pagination).isNull()
            // при наличии результата
//            assertThat(result).isNotEmpty
//            assertThat(pagination).isNotNull
        }
    }

    @Disabled("launched storage service is needed")
    @Test
    fun getObject() {
        runBlocking {
            createObject()
            val result = client.getObject(
                key = KEY,
            )
            println(result?.toString(Charset.defaultCharset()))
            assertThat(result).isEqualTo(OBJ)
        }
    }

    private suspend fun createObject() {
        client.postObject(
            key = KEY,
            obj = OBJ,
        )
    }

    @Disabled("launched storage service is needed")
    @Test
    fun `getObject by not existing key`() {
        runBlocking {
            val result = client.getObject(
                key = "any",
            )
            println(result?.toString(Charset.defaultCharset()))
            assertThat(result).isNull()
        }
    }

    @Disabled("launched storage service is needed")
    @Test
    fun postObject() {
        runBlocking {
            createObject()
        }
    }

    @Disabled("launched storage service is needed")
    @Test
    fun deleteObject() {
        runBlocking {
            createObject()
            var getObjectResult = client.getObject(
                key = KEY,
            )
            println(getObjectResult?.toString(Charset.defaultCharset()))
            assertThat(getObjectResult).isNotNull

            client.deleteObject(
                key = KEY,
            )
            getObjectResult = client.getObject(
                    key = KEY,
            )
            println(getObjectResult?.toString(Charset.defaultCharset()))
            assertThat(getObjectResult).isNull()
        }
    }

    @Disabled("launched storage service is needed")
    @Test
    fun startMultipartUpload() {
        runBlocking {
            createObject()
            val result = client.startMultipartUpload(
                key = KEY,
            )
            println(result)
            assertThat(result).isNotEmpty
        }
    }

    @Test
    fun `startMultipartUpload by not existing key`() {
        runBlocking {
            val result = client.startMultipartUpload(
                key = "notExisting",
            )
            println(result)
            assertThat(result).isNull()
        }
    }

    @Disabled("launched storage service is needed")
    @Test
    fun `getStatusMultipartUpload without parts`() {
        runBlocking {
            createObject()
            val upload = client.startMultipartUpload(
                key = KEY,
            )
            assertThat(upload).isNotEmpty
            val result = client.getStatusMultipartUpload(
                key = KEY,
                upload = upload!!,
            )
            println(result)
            // если не было загружено ни одной части, то возвращается null
            assertThat(result).isNull()
        }
    }

    @Disabled("launched storage service is needed")
    @Test
    fun getStatusMultipartUpload() {
        runBlocking {
            val (upload, _) = createObjAndUploadPart()
            val result = client.getStatusMultipartUpload(
                key = KEY,
                upload = upload,
            )
            println(result)
            assertThat(result).isNotEmpty
        }
    }

    @Disabled("launched storage service is needed")
    @Test
    fun getMultipartUploadsInProgress() {
        runBlocking {
            // todo не готово на серверной части
            val (result, pagination) = client.getMultipartUploadsInProgress(
                key = KEY,
                limit = 10,
                offset = 1,
            )
            println(result)
            // если не найдено
            assertThat(result).isNull()
            assertThat(pagination).isNull()
            // при наличии результата
//            assertThat(result).isNotEmpty
//            assertThat(pagination).isNotNull
        }
    }

    @Disabled("launched storage service is needed")
    @Test
    fun completeMultipartUpload() {
        runBlocking {
            val (upload, _) = createObjAndUploadPart()
            client.completeMultipartUpload(
                key = KEY,
                upload = upload,
            )
        }
    }

    @Disabled("launched storage service is needed")
    @Test
    fun abortMultipartUpload() {
        runBlocking {
            val (upload, _) = createObjAndUploadPart()
            client.abortMultipartUpload(
                key = KEY,
                upload = upload,
            )
        }
    }

    @Disabled("launched storage service is needed")
    @Test
    fun uploadPart() {
        runBlocking {
            val (upload, result) = createObjAndUploadPart()
            println(result)
            // На серверной части заполняется пустая строка, после конвертаций она приходит null.
            // То есть при несуществующем объекте и при успешной загрузке результат одинаковый = null
            assertThat(result).isNull()
        }
    }

    private suspend fun createObjAndUploadPart(): Pair<String, String?> {
        createObject()
        val upload = client.startMultipartUpload(
            key = KEY,
        )
        assertThat(upload).isNotEmpty
        val result = client.uploadPart(
            key = KEY,
            upload = upload!!,
            part = 1,
            obj = "added".toByteArray()
        )
        return Pair(upload, result)
    }

    @Disabled("launched storage service is needed")
    @Test
    fun getObjectMeta() {
        runBlocking {
            createObject()
            val result = client.getObjectMeta(
                key = KEY,
            )
            println(result)
            assertThat(result).isNotNull
        }
    }

    @Disabled("launched storage service is needed")
    @Test
    fun getPresignedUrl() {
        runBlocking {
            val result = client.getPresignedUrl(
                key = KEY,
                body = GetPresignedUrlBody(
                    prefix = true,
                    access = listOf(READ),
                    expiration = "2h"
                )
            )
            println(result)
            // дает результат даже при отсутствии объекта
            assertThat(result).isNotBlank
        }
    }

    @Disabled("launched storage service is needed")
    @Test
    fun createPostFormForUpload() {
        runBlocking {
            client.createPostFormForUpload(
                key = KEY,
            )
            // todo еще не поддержано на стороне сервера
        }
    }

    @Disabled("launched storage service is needed")
    @Test
    fun listJobs() {
        runBlocking {
            client.listJobs()
            // todo еще не поддержано на стороне сервера
        }
    }

    @Disabled("launched storage service is needed")
    @Test
    fun createJob() {
        runBlocking {
            client.createJob()
            // todo еще не поддержано на стороне сервера
        }
    }

    @Disabled("launched storage service is needed")
    @Test
    fun getJobMetaData() {
        runBlocking {
            client.getJobMetaData(
                job = JOB,
            )
            // todo еще не поддержано на стороне сервера
        }
    }

    @Disabled("launched storage service is needed")
    @Test
    fun deleteJob() {
        runBlocking {
            client.deleteJob(
                job = JOB,
            )
            // todo еще не поддержано на стороне сервера
        }
    }

    private companion object {
        const val KEY = "documents/123"
        const val JOB = "7"
        val OBJ = "мы текст, с нами буквы".toByteArray()
    }
}