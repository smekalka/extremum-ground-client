package io.extremum.ground.client.url

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ApiUrlHolderTest {

    @Test
    fun api() {
        val withHttps = ApiUrlHolder(baseUrl="https://api.ajev84.y.extremum.io",xAppId= "123",path="/v3").apiUrl
        assertThat(withHttps).isEqualTo("https://api.app-123.ajev84.y.extremum.io/v3")

        val withHttp = ApiUrlHolder(baseUrl= "http://api.ajev84.y.extremum.io",xAppId= "123",path= "/v3").apiUrl
        assertThat(withHttp).isEqualTo("http://api.app-123.ajev84.y.extremum.io/v3")

        val withUri = ApiUrlHolder(uri = "http://my.compony.org", baseUrl = "any", xAppId = "any",path= "/v3").apiUrl
        assertThat(withUri).isEqualTo("http://my.compony.org/v3")
    }
}