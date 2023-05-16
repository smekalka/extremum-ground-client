package io.extremum.ground.client.storage.requestbody

import com.fasterxml.jackson.annotation.JsonProperty
import io.extremum.model.tools.api.StringUtils.validateExpiration

data class GetPresignedUrlBody(
    val prefix: Boolean = false,
    val access: List<Access> = listOf(),
    /**
     * Истечение в виде продолжительности.
     * Формат см. [validateExpiration]
     */
    val expiration: String,
) {
    enum class Access {
        @JsonProperty("read")
        READ,

        @JsonProperty("write")
        WRITE,
    }

    init {
        expiration.validateExpiration()
    }
}