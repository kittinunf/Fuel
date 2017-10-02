package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.Encoding
import com.github.kittinunf.fuel.core.Method
import org.junit.Assert.assertThat
import org.junit.Test
import java.io.ByteArrayOutputStream
import org.hamcrest.CoreMatchers.`is` as isEqualTo

class EncodingTest : BaseTestCase() {

    @Test
    fun testEncodingNormal() {
        val request = Encoding(
                httpMethod = Method.GET,
                baseUrlString = "http://www.example.com",
                urlString = "test",
                parameters = listOf("a" to "b")
        ).request

        assertThat(request.url.toString(), isEqualTo("http://www.example.com/test?a=b"))
    }

    @Test
    fun testEncodingWithNoParam() {
        val request = Encoding(
                httpMethod = Method.GET,
                baseUrlString = "http://www.example.com",
                urlString = "test",
                parameters = null
        ).request

        assertThat(request.url.toString(), isEqualTo("http://www.example.com/test"))
    }

    @Test
    fun testEncodingWithIntegerParameter() {
        val request = Encoding(
                httpMethod = Method.GET,
                baseUrlString = "http://www.example.com",
                urlString = "test",
                parameters = listOf("foo" to 1)
        ).request

        assertThat(request.url.toString(), isEqualTo("http://www.example.com/test?foo=1"))
    }

    @Test
    fun testEncodingWithDoubleParameter() {
        val request = Encoding(
                httpMethod = Method.GET,
                baseUrlString = "http://www.example.com",
                urlString = "test",
                parameters = listOf("foo" to 1.1)
        ).request

        assertThat(request.url.toString(), isEqualTo("http://www.example.com/test?foo=1.1"))
    }

    @Test
    fun testEncodingWithBooleanParameter() {
        val request = Encoding(
                httpMethod = Method.GET,
                baseUrlString = "http://www.example.com",
                urlString = "test",
                parameters = listOf("foo" to true)
        ).request

        assertThat(request.url.toString(), isEqualTo("http://www.example.com/test?foo=true"))
    }

    @Test
    fun testEncodingWithNoParamAndNoRelativeUrlString() {
        val request = Encoding(
                httpMethod = Method.GET,
                baseUrlString = "http://www.example.com/test",
                urlString = "",
                parameters = null
        ).request

        assertThat(request.url.toString(), isEqualTo("http://www.example.com/test"))
    }

    @Test
    fun testEncodingWithJustBaseUrlString() {
        val request = Encoding(
                httpMethod = Method.GET,
                baseUrlString = "http://www.example.com/test?a=b",
                urlString = "",
                parameters = null
        ).request

        assertThat(request.url.toString(), isEqualTo("http://www.example.com/test?a=b"))
    }

    @Test
    fun testEncodingWithPercentEscapedCharacterPath() {
        val request = Encoding(
                httpMethod = Method.GET,
                baseUrlString = "http://www.example.com/U$3|2|\\|@me/P@$\$vv0|2|)",
                urlString = "",
                parameters = null
        ).request

        assertThat(request.url.toString(), isEqualTo("http://www.example.com/U$3%7C2%7C%5C%7C@me/P@$\$vv0%7C2%7C)"))
    }

    @Test
    fun testEncodingAlreadyEncodedUrl() {
        val supposed = "https://www.example.com/files/%D7%98%D7%A7%D7%A1%D7%98%20%D7%91%D7%A2%D7%91%D7%A8%D7%99%D7%AA%201%203.txt"
        val request = Encoding(
                httpMethod = Method.GET,
                urlString = supposed
        ).request

        assertThat(request.url.toString(), isEqualTo(supposed))
    }

    @Test
    fun testEncodingNonAsciiString() {
        val hebrewString = "טקסט בעברית 1 3.txt"
        val path = "https://www.example.com/files/" + hebrewString

        val request = Encoding(
                httpMethod = Method.GET,
                urlString = path,
                parameters = null
        ).request

        val supposed = "https://www.example.com/files/%D7%98%D7%A7%D7%A1%D7%98%20%D7%91%D7%A2%D7%91%D7%A8%D7%99%D7%AA%201%203.txt"
        assertThat(request.url.toString(), isEqualTo(supposed))
    }

    @Test
    fun testPostRequestEncodingWithoutReservedCharacters() {
        val path = "https://www.example.com/files"
        val parameters = listOf("param1" to "value1", "param2" to "value2")
        val request = Encoding(
                httpMethod = Method.POST,
                urlString = path,
                parameters = parameters
        ).request

        val body = ByteArrayOutputStream().apply {
            request.bodyCallback?.invoke(request, this, 0)
        }.toByteArray()

        val bodyString = String(body)
        print(bodyString)
        assertThat(bodyString, isEqualTo("param1=value1&param2=value2"))
    }

    @Test
    fun testPostRequestEncodingWithReservedCharacters() {
        val path = "https://www.example.com/files"
        val parameters = listOf("param1" to "val+ue", "param2" to "val ue",
                "param3" to "val!ue" ,"param4" to ":/?#[]@$&'()*+,;= ")
        val request = Encoding(
                httpMethod = Method.POST,
                urlString = path,
                parameters = parameters
        ).request

        val body = ByteArrayOutputStream().apply {
            request.bodyCallback?.invoke(request, this, 0)
        }.toByteArray()

        val bodyString = String(body)
        assertThat(bodyString, isEqualTo("param1=val%2Bue&param2=val+ue&param3=val%21ue&param4=%3A%2F%3F%23%5B%5D%40%24%26%27%28%29*%2B%2C%3B%3D+"))
    }

}
