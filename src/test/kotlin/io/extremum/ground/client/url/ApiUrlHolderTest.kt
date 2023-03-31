package io.extremum.ground.client.url

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ApiUrlHolderTest {

    @Test
    fun api() {
        val withHttps = ApiUrlHolder("https://api.ajev84.y.extremum.io", "123", "/v3").apiUrl
        assertThat(withHttps).isEqualTo("https://api.app-123.ajev84.y.extremum.io/v3")

        val withHttp = ApiUrlHolder("http://api.ajev84.y.extremum.io", "123", "/v3").apiUrl
        assertThat(withHttp).isEqualTo("http://api.app-123.ajev84.y.extremum.io/v3")
    }
}