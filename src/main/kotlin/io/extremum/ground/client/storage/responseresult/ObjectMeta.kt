package io.extremum.ground.client.storage.responseresult

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.ZonedDateTime

data class ObjectMeta(
    @JsonProperty("@id")
    val id: String,
    @JsonProperty("@uuid")
    val uuid: String? = null,
    @JsonProperty("@type")
    val type: String,
    @JsonProperty("@created")
    val created: ZonedDateTime? = null,
    @JsonProperty("@updated")
    val updated: ZonedDateTime? = null,
    @JsonProperty("@version")
    val version: Int? = null,
    val key: String? = null,
    val contentType: String? = null,
    val size: Float? = null,
)