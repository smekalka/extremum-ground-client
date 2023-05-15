package io.extremum.ground.client.storage.requestbody

import com.fasterxml.jackson.annotation.JsonProperty

data class GetPresignedUrlBody(
    val prefix: Boolean = false,
    val access: List<Access> = listOf(),
    /**
     * Истечение в виде продолжительности.
     * Формат: набор целых или дробных чисел с опциональным знаком и единицей измерения. Например, "300ms", "-1.5h" or "2h45m".
     * Возможные единицы времени: "ns", "us" (или "µs"), "ms", "s", "m", "h"
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

    companion object {
        /**
         * Исходный regex взят из /libexec/src/time/format.go:ParseDuration "[-+]?([0-9]*(\\.[0-9]*)?[a-z]+)+"
         */
        private val validExpirationRegex = "[-+]?([0-9]+(\\.[0-9]*)?(ns|us|µs|ms|s|m|h)+)+".toRegex()

        private fun String.validateExpiration(): String {
            if (!this.matches(validExpirationRegex)) {
                throw IllegalStateException("Expiration '$this' does not match the pattern '$validExpirationRegex'")
            }
            return this
        }
    }
}