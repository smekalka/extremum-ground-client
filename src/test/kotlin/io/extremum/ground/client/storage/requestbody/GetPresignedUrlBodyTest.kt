package io.extremum.ground.client.storage.requestbody

import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.lang.IllegalStateException

class GetPresignedUrlBodyTest {

    @ParameterizedTest
    @MethodSource("validGetPresignedUrlBodyExpiration")
    fun `validate GetPresignedUrlBody expiration`(case: TestCase) {
        with(case) {
            if (!exp) {
                assertThrows<IllegalStateException> {
                    GetPresignedUrlBody(expiration = value)
                }
            } else {
                GetPresignedUrlBody(expiration = value)
            }
        }
    }

    private companion object {
        @Suppress("unused")
        @JvmStatic
        fun validGetPresignedUrlBodyExpiration() = arrayOf(
            arrayOf(TestCase(value = "300ms", exp = true)),
            arrayOf(TestCase(value = "+300ms", exp = true)),
            arrayOf(TestCase(value = "-1.5h", exp = true)),
            arrayOf(TestCase(value = "2h45m", exp = true)),
            arrayOf(TestCase(value = "2t45m", exp = false)),
            arrayOf(TestCase(value = "s45m", exp = false)),
            arrayOf(TestCase(value = "--1.5h", exp = false)),
        )
    }

    data class TestCase(
        val desc: String = "",
        val value: String,
        val exp: Boolean
    )
}