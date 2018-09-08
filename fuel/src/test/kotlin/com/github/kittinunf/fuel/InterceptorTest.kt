package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.interceptors.cUrlLoggingRequestInterceptor
import com.github.kittinunf.fuel.core.interceptors.loggingRequestInterceptor
import com.github.kittinunf.fuel.core.interceptors.loggingResponseInterceptor
import com.github.kittinunf.fuel.core.interceptors.validatorResponseInterceptor
import org.hamcrest.CoreMatchers.*
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

        assertThat(response.statusCode, isEqualTo(HttpURLConnection.HTTP_OK))
    }

    @Test
    fun testWithLoggingRequestInterceptor() {
        val manager = FuelManager()
        manager.addRequestInterceptor(loggingRequestInterceptor())

        val (request, response, result) = manager.request(Method.GET, "https://httpbin.org/get").response()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        assertThat(response.statusCode, isEqualTo(HttpURLConnection.HTTP_OK))
        manager.removeRequestInterceptor(loggingRequestInterceptor())
    }

    @Test
    fun testWithLoggingResponseInterceptor() {
        val manager = FuelManager()
        manager.addResponseInterceptor { loggingResponseInterceptor() }

        val (request, response, result) = manager.request(Method.GET, "https://httpbin.org/get").response()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        assertThat(response.statusCode, isEqualTo(HttpURLConnection.HTTP_OK))
        manager.removeResponseInterceptor { loggingResponseInterceptor() }
    }

    @Test
    fun testWithResponseToString() {
        val manager = FuelManager()
        manager.addResponseInterceptor { loggingResponseInterceptor() }

        val (request, response, result) = manager.request(Method.GET, "https://httpbin.org/get").response()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        assertThat(response.statusCode, isEqualTo(HttpURLConnection.HTTP_OK))

        assertThat(response.toString(), containsString("Response :"))
        assertThat(response.toString(), containsString("Length :"))
        assertThat(response.toString(), containsString("Body :"))
        assertThat(response.toString(), containsString("Headers :"))
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

        assertThat(response.statusCode, isEqualTo(HttpURLConnection.HTTP_OK))
        assertThat(interceptorCalled, isEqualTo(true))
    }

    @Test
    fun testWithBreakingChainInterceptor() {
        val manager = FuelManager()

        var interceptorCalled = false
        fun <T> customLoggingBreakingInterceptor() = { _: (T) -> T ->
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

        assertThat(response.statusCode, isEqualTo(HttpURLConnection.HTTP_OK))
        assertThat(interceptorCalled, isEqualTo(true))
        assertThat(interceptorNotCalled, isEqualTo(true))
    }

    @Test
    fun testWithRedirectInterceptor() {
        val manager = FuelManager()

        manager.addRequestInterceptor(cUrlLoggingRequestInterceptor())

        val (request, response, result) = manager.request(Method.GET,
                "https://httpbin.org/redirect-to",
                listOf("url" to "http://www.google.com"))
                .header(mapOf("User-Agent" to "Fuel"))
                .response()

        val (data, error) = result
        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        assertThat(response.statusCode, isEqualTo(HttpURLConnection.HTTP_OK))
    }

    @Test
    fun testWithoutDefaultRedirectionInterceptor() {
        val manager = FuelManager()
        manager.addRequestInterceptor(cUrlLoggingRequestInterceptor())
        manager.removeAllResponseInterceptors()

        val (request, response, result) = manager.request(Method.GET,
                "https://httpbin.org/relative-redirect/3")
                .header(mapOf("User-Agent" to "Fuel"))
                .response()

        val (data, error) = result
        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        assertThat(response.statusCode, isEqualTo(HttpURLConnection.HTTP_MOVED_TEMP))
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

        assertThat(response.statusCode, isEqualTo(HttpURLConnection.HTTP_OK))
    }

    @Test
    fun testWithRedirectInterceptorPreservesBaseHeaders() {
        val manager = FuelManager()
        manager.addRequestInterceptor(cUrlLoggingRequestInterceptor())

        manager.baseHeaders = mapOf("User-Agent" to "Fuel")
        val (request, response, result) = manager.request(Method.GET,
                "https://httpbin.org/redirect-to?url=/user-agent")
                .responseString(Charsets.UTF_8)

        val (data, error) = result
        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, containsString("\"user-agent\": \"Fuel\""))

        assertThat(response.statusCode, isEqualTo(HttpURLConnection.HTTP_OK))
    }

    @Test
    fun testNestedRedirectWithRedirectInterceptor() {
        val manager = FuelManager()

        manager.addRequestInterceptor(cUrlLoggingRequestInterceptor())

        val (request, response, result) = manager.request(Method.GET,
                "https://httpbin.org/redirect-to",
                listOf("url" to "https://httpbin.org/redirect-to?url=http://www.google.com"))
                .header(mapOf("User-Agent" to "Fuel"))
                .response()

        val (data, error) = result
        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        assertThat(response.statusCode, isEqualTo(HttpURLConnection.HTTP_OK))
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

        assertThat(response.statusCode, isEqualTo(418))
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
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        assertThat(response.statusCode, isEqualTo(418))
    }

    @Test
    fun failsIfRequestedResourceReturns404() {
        val manager = FuelManager()
        val (_, _, result) = manager.request(Method.GET, "http://httpbin.org/status/404").response()
        val (data, error) = result

        assertThat(error, notNullValue())
        assertThat(data, nullValue())
    }

    @Test
    fun failsIfRedirectedToResourceReturning404() {
        val manager = FuelManager()
        val (_, _, result) = manager.request(Method.GET,
                "http://httpbin.org/redirect-to",
                listOf("url" to "http://httpbin.org/status/404"))
                .header(mapOf("User-Agent" to "Fuel"))
                .response()
        val (data, error) = result

        assertThat(error, notNullValue())
        assertThat(data, nullValue())
    }

    @Test
    fun testGet301Redirect() {
        val manager = FuelManager()

        val (_, _, result) = manager.request(Method.GET,
                "http://httpbin.org/redirect-to",
                listOf("url" to "http://httpbin.org/get", "status_code" to HttpURLConnection.HTTP_MOVED_PERM))
                .responseString()

        val (data, error) = result

        assertThat(data, notNullValue())
        assertThat(data, containsString("http://httpbin.org/get"))
        assertThat(error, nullValue())
    }

    @Test
    fun testGet302Redirect() {
        val manager = FuelManager()

        val (_, _, result) = manager.request(Method.GET,
                "http://httpbin.org/redirect-to",
                listOf("url" to "http://httpbin.org/get"))
                .responseString()

        val (data, error) = result

        assertThat(data, notNullValue())
        assertThat(data, containsString("http://httpbin.org/get"))
        assertThat(error, nullValue())
    }

    @Test
    fun testGet303Redirect() {
        val manager = FuelManager()

        val (_, _, result) = manager.request(Method.GET,
                "http://httpbin.org/redirect-to",
                listOf("url" to "http://httpbin.org/get", "status_code" to HttpURLConnection.HTTP_SEE_OTHER))
                .responseString()

        val (data, error) = result

        assertThat(data, notNullValue())
        assertThat(data, containsString("http://httpbin.org/get"))
        assertThat(error, nullValue())
    }

    @Test
    fun testGetOthersRedirect() {
        val manager = FuelManager()

        val (_, _, result) = manager.request(Method.GET,
                "http://httpbin.org/redirect-to",
                listOf("url" to "http://httpbin.org/get", "status_code" to HttpURLConnection.HTTP_NOT_MODIFIED))
                .responseString()

        val (data, error) = result

        assertThat(data, notNullValue())
        assertThat(data, containsString("http://httpbin.org/get"))
        assertThat(error, nullValue())
    }

    @Test
    fun testGetRedirectNoUrl() {
        val manager = FuelManager()

        val (_, _, result) = manager.request(Method.GET,
                "http://httpbin.org/redirect-to")
                .responseString()

        val (data, error) = result

        assertThat(data, nullValue())
        assertThat(data, not(containsString("http://httpbin.org/get")))
        assertThat(error, notNullValue())
    }

    @Test
    fun testGetWrongUrl() {
        val manager = FuelManager()

        val (_, _, result) = manager.request(Method.GET,
                "http://httpbin.org/redirect-to",
                listOf("url" to "http://ww"))
                .responseString()

        val (data, error) = result

        assertThat(data, nullValue())
        assertThat(data, not(containsString("http://httpbin.org/get")))
        assertThat(error, notNullValue())
    }

    @Test
    fun testPost301Redirect() {
        val manager = FuelManager()
        val requests = mutableListOf<Request>()

        manager.addRequestInterceptor { next: (Request) -> Request ->
            { r: Request ->
                requests.add(r)
                next(r)
            }
        }

        val (originalRequest, _, result) = manager.request(Method.POST,
                "http://httpstat.us/301")
                .responseString()

        val (data, error) = result

        assertThat(data, notNullValue())
        assertThat(error, nullValue())
        assertThat(originalRequest.method, isEqualTo(Method.POST))
        assertThat(requests[1].method, isEqualTo(Method.GET))
    }

    @Test
    fun testPost302Redirect() {
        val manager = FuelManager()
        val requests = mutableListOf<Request>()

        manager.addRequestInterceptor { next: (Request) -> Request ->
            { r: Request ->
                requests.add(r)
                next(r)
            }
        }

        val (originalRequest, _, result) = manager.request(Method.POST,
                "http://httpstat.us/302")
                .responseString()

        val (data, error) = result

        assertThat(data, notNullValue())
        assertThat(error, nullValue())
        assertThat(originalRequest.method, isEqualTo(Method.POST))
        assertThat(requests[1].method, isEqualTo(Method.GET))
    }

    @Test
    fun testPost303Redirect() {
        val manager = FuelManager()
        val requests = mutableListOf<Request>()

        manager.addRequestInterceptor { next: (Request) -> Request ->
            { r: Request ->
                requests.add(r)
                next(r)
            }
        }

        val (originalRequest, _, result) = manager.request(Method.POST,
                "http://httpstat.us/303")
                .responseString()

        val (data, error) = result

        assertThat(data, notNullValue())
        assertThat(error, nullValue())
        assertThat(originalRequest.method, isEqualTo(Method.POST))
        assertThat(requests[1].method, isEqualTo(Method.GET))
    }

    @Test
    fun testPost307Redirect() {
        val manager = FuelManager()
        val requests = mutableListOf<Request>()

        manager.addRequestInterceptor { next: (Request) -> Request ->
            { r: Request ->
                requests.add(r)
                next(r)
            }
        }

        val (originalRequest, _, result) = manager.request(Method.POST,
                "http://httpstat.us/307")
                .responseString()

        val (data, error) = result

        assertThat(data, notNullValue())
        assertThat(error, nullValue())
        assertThat(originalRequest.method, isEqualTo(Method.POST))
        assertThat(requests[1].method, isEqualTo(Method.POST))
    }

    @Test
    fun testPost308Redirect() {
        val manager = FuelManager()

        val requests = mutableListOf<Request>()

        manager.addRequestInterceptor { next: (Request) -> Request ->
            { r: Request ->
                requests.add(r)
                next(r)
            }
        }

        val (originalRequest, _, result) = manager.request(Method.POST,
                "http://httpstat.us/308")
                .responseString()

        val (data, error) = result

        assertThat(data, notNullValue())
        assertThat(error, nullValue())
        assertThat(originalRequest.method, isEqualTo(Method.POST))
        assertThat(requests[1].method, isEqualTo(Method.POST))
    }

    @Test
    fun testHeaderIsPassingAlongWithRedirection() {
        val manager = FuelManager()

        val (_, _, result) = manager.request(Method.GET,
                "http://httpbin.org/redirect-to",
                listOf("url" to "http://httpbin.org/get"))
                .header("Foo" to "bar")
                .responseString()

        val (data, error) = result

        assertThat(data, notNullValue())
        assertThat(data, containsString("http://httpbin.org/get"))
        assertThat(data, containsString("\"Foo\": \"bar"))
        assertThat(error, nullValue())
    }

    @Test
    fun testHeaderIsPassingAlongWithRedirectionWithinSubPath() {
        val manager = FuelManager()

        val (_, _, result) = manager.request(Method.GET,
                "http://httpbin.org/redirect-to",
                listOf("url" to "/basic-auth/user/pass"))
                .header("Foo" to "bar")
                .authenticate("user", "pass")
                .responseString()

        val (data, error) = result

        println(error)
        assertThat(data, notNullValue())
        assertThat(error, nullValue())
    }

    @Test
    fun testHeaderAuthenticationWillBeRemoveIfRedirectToDifferentHost() {
        val manager = FuelManager()

        val requests = mutableListOf<Request>()

        manager.addRequestInterceptor { next: (Request) -> Request ->
            { r: Request ->
                requests.add(r)
                next(r)
            }
        }

        val (_, _, result) = manager.request(Method.GET,
                "http://httpbin.org/redirect-to",
                listOf("url" to "http://httpstat.us"))
                .authenticate("foo", "bar")
                .header("Foo" to "bar")
                .responseString()


        val (data, error) = result

        assertThat(data, notNullValue())
        assertThat(error, nullValue())
        assertThat(requests[1].headers["Foo"], notNullValue())
        assertThat(requests[1].headers["Authorization"], nullValue())
    }

    @Test
    fun testDoNotAllowRedirect() {
        val manager = FuelManager()

        val (_, _, result) = manager.request(Method.GET,
                "http://httpbin.org/redirect-to",
                listOf("url" to "/get"))
                .allowRedirects(false)
                .responseString()

        val (data, error) = result

        // TODO: This is current based on the current behavior, however we need to fix this as it should handle 100 - 399 gracefully not httpException
        assertThat(data, nullValue())
        assertThat(error, notNullValue())
    }

    @Test
    fun testHttpExceptionWithRemoveAllRequestInterceptors() {
        val manager = FuelManager()
        manager.removeAllRequestInterceptors()

        val (request, response, result) = manager.request(Method.GET,
                "http://httpbin.org/status/418").response()

        val (data, error) = result
        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        assertThat(response.statusCode, isEqualTo(418))
    }
}
