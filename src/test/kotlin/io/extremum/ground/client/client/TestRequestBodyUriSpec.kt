package io.extremum.ground.client.client

import org.reactivestreams.Publisher
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ClientHttpRequest
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.BodyInserter
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriBuilder
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.util.context.Context
import java.net.URI
import java.nio.charset.Charset
import java.time.ZonedDateTime
import java.util.function.Consumer
import java.util.function.Function

class TestRequestBodyUriSpec(var response: ClientResponse) : WebClient.RequestBodyUriSpec {

    override fun <V : Any?> exchangeToMono(responseHandler: Function<ClientResponse, out Mono<V>>): Mono<V> {
        return responseHandler.apply(response)
            .flatMap { value: V ->
                Mono.just(value)
            }
            .switchIfEmpty(Mono.defer {
                Mono.empty()
            })
    }

    override fun cookie(name: String, value: String): WebClient.RequestBodySpec {
        return this
    }

    override fun cookies(cookiesConsumer: Consumer<MultiValueMap<String, String>>): WebClient.RequestBodySpec {
        return this
    }

    override fun header(headerName: String, vararg headerValues: String?): WebClient.RequestBodySpec {
        return this
    }

    override fun headers(headersConsumer: Consumer<HttpHeaders>): WebClient.RequestBodySpec {
        return this
    }

    override fun accept(vararg acceptableMediaTypes: MediaType?): WebClient.RequestBodySpec {
        notImplementedError()
    }

    override fun acceptCharset(vararg acceptableCharsets: Charset?): WebClient.RequestBodySpec {
        notImplementedError()
    }

    override fun ifModifiedSince(ifModifiedSince: ZonedDateTime): WebClient.RequestBodySpec {
        notImplementedError()
    }

    override fun ifNoneMatch(vararg ifNoneMatches: String?): WebClient.RequestBodySpec {
        notImplementedError()
    }

    override fun attribute(name: String, value: Any): WebClient.RequestBodySpec {
        notImplementedError()
    }

    override fun attributes(attributesConsumer: Consumer<MutableMap<String, Any>>): WebClient.RequestBodySpec {
        notImplementedError()
    }

    override fun context(contextModifier: Function<Context, Context>): WebClient.RequestBodySpec {
        notImplementedError()
    }

    override fun httpRequest(requestConsumer: Consumer<ClientHttpRequest>): WebClient.RequestBodySpec {
        notImplementedError()
    }

    override fun retrieve(): WebClient.ResponseSpec {
        notImplementedError()
    }

    override fun <V : Any?> exchangeToFlux(responseHandler: Function<ClientResponse, out Flux<V>>): Flux<V> {
        notImplementedError()
    }

    override fun exchange(): Mono<ClientResponse> {
        notImplementedError()
    }

    override fun contentLength(contentLength: Long): WebClient.RequestBodySpec {
        notImplementedError()
    }

    override fun contentType(contentType: MediaType): WebClient.RequestBodySpec {
        notImplementedError()
    }

    override fun bodyValue(body: Any): WebClient.RequestHeadersSpec<*> {
        notImplementedError()
    }

    override fun <T : Any?, P : Publisher<T>?> body(
        publisher: P,
        elementClass: Class<T>
    ): WebClient.RequestHeadersSpec<*> {
        notImplementedError()
    }

    override fun <T : Any?, P : Publisher<T>?> body(
        publisher: P,
        elementTypeRef: ParameterizedTypeReference<T>
    ): WebClient.RequestHeadersSpec<*> {
        notImplementedError()
    }

    override fun body(producer: Any, elementClass: Class<*>): WebClient.RequestHeadersSpec<*> {
        notImplementedError()
    }

    override fun body(
        producer: Any,
        elementTypeRef: ParameterizedTypeReference<*>
    ): WebClient.RequestHeadersSpec<*> {
        notImplementedError()
    }

    override fun body(inserter: BodyInserter<*, in ClientHttpRequest>): WebClient.RequestHeadersSpec<*> {
        notImplementedError()
    }

    override fun syncBody(body: Any): WebClient.RequestHeadersSpec<*> {
        notImplementedError()
    }

    override fun uri(uri: URI): WebClient.RequestBodySpec {
        notImplementedError()
    }

    override fun uri(uri: String, vararg uriVariables: Any?): WebClient.RequestBodySpec {
        notImplementedError()
    }

    override fun uri(uri: String, uriVariables: MutableMap<String, *>): WebClient.RequestBodySpec {
        notImplementedError()
    }

    override fun uri(uri: String, uriFunction: Function<UriBuilder, URI>): WebClient.RequestBodySpec {
        notImplementedError()
    }

    override fun uri(uriFunction: Function<UriBuilder, URI>): WebClient.RequestBodySpec {
        notImplementedError()
    }

    private fun notImplementedError(): Nothing {
        throw NotImplementedError("An operation is not implemented for tests")
    }
}