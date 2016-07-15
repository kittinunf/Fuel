package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.interceptors.cUrlLoggingRequestInterceptor
import com.github.kittinunf.fuel.core.interceptors.loggingInterceptor
import com.github.kittinunf.fuel.core.interceptors.loggingResponseInterceptor
import com.github.kittinunf.fuel.core.interceptors.validatorResponseInterceptor
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Assert.assertThat
import org.junit.Test
import java.net.HttpURLConnection
import org.hamcrest.CoreMatchers.`is` as isEqualTo

class InterceptorTest : BaseTestCase() {

    @Test
    fun testWithNoInterceptor() {
        val manager = FuelManager()
        val (request, response, result) = manager.request(Method.GET, "https://httpbin.org/get").response()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        assertThat(response.httpStatusCode, isEqualTo(HttpURLConnection.HTTP_OK))
    }

    @Test
    fun testWithLoggingInterceptor() {
        val manager = FuelManager()
        manager.addRequestInterceptor(loggingInterceptor())

        val (request, response, result) = manager.request(Method.GET, "https://httpbin.org/get").response()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        assertThat(response.httpStatusCode, isEqualTo(HttpURLConnection.HTTP_OK))
    }

    @Test
    fun testWithMultipleInterceptors() {
        val manager = FuelManager()

        var interceptorCalled = false

        fun <T> customLoggingInterceptor() = { next: (T) -> T ->
            { t: T ->
                println("1: ${t.toString()}")
                interceptorCalled = true
                next(t)
            }
        }

        manager.apply {
            addRequestInterceptor(cUrlLoggingRequestInterceptor())
            addRequestInterceptor(customLoggingInterceptor())
        }

        val (request, response, result) = manager.request(Method.GET, "https://httpbin.org/get").header(mapOf("User-Agent" to "Fuel")).response()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        assertThat(response.httpStatusCode, isEqualTo(HttpURLConnection.HTTP_OK))
        assertThat(interceptorCalled, isEqualTo(true))
    }

    @Test
    fun testWithBreakingChainInterceptor() {
        val manager = FuelManager()

        var interceptorCalled = false
        fun <T> customLoggingBreakingInterceptor() = { next: (T) -> T ->
            { t: T ->
                println("1: ${t.toString()}")
                interceptorCalled = true
                //if next is not called, next Interceptor will not be called as well
                t
            }
        }

        var interceptorNotCalled = true
        fun <T> customLoggingInterceptor() = { next: (T) -> T ->
            { t: T ->
                println("1: ${t.toString()}")
                interceptorNotCalled = false
                next(t)
            }
        }

        manager.apply {
            addRequestInterceptor(cUrlLoggingRequestInterceptor())
            addRequestInterceptor(customLoggingBreakingInterceptor())
            addRequestInterceptor(customLoggingInterceptor())
        }

        val (request, response, result) = manager.request(Method.GET, "https://httpbin.org/get").header(mapOf("User-Agent" to "Fuel")).response()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        assertThat(response.httpStatusCode, isEqualTo(HttpURLConnection.HTTP_OK))
        assertThat(interceptorCalled, isEqualTo(true))
        assertThat(interceptorNotCalled, isEqualTo(true))
    }

    @Test
    fun testWithRedirectInterceptor() {
        val manager = FuelManager()

        manager.addRequestInterceptor(cUrlLoggingRequestInterceptor())

        val (request, response, result) = manager.request(Method.GET,
                "https://httpbin.org/redirect-to",
                listOf("url" to "http://www.example.com"))
                .header(mapOf("User-Agent" to "Fuel"))
                .response()

        val (data, error) = result
        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        assertThat(response.httpStatusCode, isEqualTo(HttpURLConnection.HTTP_OK))
    }

    @Test
    fun testWithRedirectInterceptorRelative() {
        val manager = FuelManager()

        manager.addRequestInterceptor(cUrlLoggingRequestInterceptor())

        val (request, response, result) = manager.request(Method.GET,
                "https://httpbin.org/relative-redirect/3")
                .header(mapOf("User-Agent" to "Fuel"))
                .response()

        val (data, error) = result
        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        assertThat(response.httpStatusCode, isEqualTo(HttpURLConnection.HTTP_OK))
    }

    @Test
    fun testNestedRedirectWithRedirectInterceptor() {
        val manager = FuelManager()

        manager.addRequestInterceptor(cUrlLoggingRequestInterceptor())

        val (request, response, result) = manager.request(Method.GET,
                "https://httpbin.org/redirect-to",
                listOf("url" to "https://httpbin.org/redirect-to?url=http://www.example.com"))
                .header(mapOf("User-Agent" to "Fuel"))
                .response()

        val (data, error) = result
        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        assertThat(response.httpStatusCode, isEqualTo(HttpURLConnection.HTTP_OK))
    }

    @Test
    fun testHttpExceptionWithValidatorInterceptor() {
        val manager = FuelManager()
        manager.addResponseInterceptor(validatorResponseInterceptor(200..299))
        manager.addRequestInterceptor(cUrlLoggingRequestInterceptor())

        val (request, response, result) = manager.request(Method.GET,
                "http://httpbin.org/status/418").response()

        val (data, error) = result
        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, notNullValue())
        assertThat(data, nullValue())

        assertThat(response.httpStatusCode, isEqualTo(418))
    }

    @Test
    fun testHttpExceptionWithRemoveInterceptors() {
        val manager = FuelManager()
        manager.removeAllResponseInterceptors()

        val (request, response, result) = manager.request(Method.GET,
                "http://httpbin.org/status/418").response()

        val (data, error) = result
        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, notNullValue())
        assertThat(data, nullValue())

        assertThat(response.httpStatusCode, isEqualTo(418))
    }

    @Test
    fun testHttpFailWithInterceptors() {
        val manager = FuelManager()

        manager.addResponseInterceptor(loggingResponseInterceptor())

        val (request, response, result) = manager.request(Method.GET, "http://httpbin.org/status").response()

        assertThat(response.httpStatusCode, isEqualTo(HttpURLConnection.HTTP_NOT_FOUND))
    }

}
 
