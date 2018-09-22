package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.FuelManager
import com.google.common.net.MediaType
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThat
import org.junit.Test
import org.mockserver.model.BinaryBody
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.util.*
import java.util.concurrent.TimeUnit
import org.hamcrest.CoreMatchers.`is` as isEqualTo

class RequestDownloadTest : MockHttpTestCase() {
    @Test
    fun httpDownloadCase() {
        val manager = FuelManager()

        val numberOfBytes = 32768
        val file = File.createTempFile(numberOfBytes.toString(), null)
        val bytes = ByteArray(numberOfBytes)
        Random().nextBytes(bytes)

        mockChain(
            request = mockRequest().withMethod(Method.GET.value).withPath("/bytes"),
            response = mockResponse().withBody(BinaryBody(bytes, MediaType.OCTET_STREAM))
        )

        val (request, response, result) = manager.download(mockPath("bytes")).destination { _, _ ->
            println(file.absolutePath)
            file
        }.response()

        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())
        assertThat(file.length(),isEqualTo(numberOfBytes.toLong()))

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpDownloadWithProgressValidCaseResponse() {
        val manager = FuelManager()

        val numberOfBytes = 1186
        val file = File.createTempFile(numberOfBytes.toString(), null)
        val bytes = ByteArray(numberOfBytes)
        Random().nextBytes(bytes)

        mockChain(
            request = mockRequest().withMethod(Method.GET.value).withPath("/bytes"),
            response = mockResponse().withBody(BinaryBody(bytes, MediaType.OCTET_STREAM))
        )

        var read = -1L
        var total = -1L

        val (request, response, result) = manager.download(mockPath("bytes")).destination { _, _ ->
            println(file.absolutePath)
            file
        }.progress { readBytes, totalBytes ->
            read = readBytes
            total = totalBytes
            println("read: $read, total: $total")
        }.response()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())
        assertEquals(data is ByteArray,true)
        assertEquals((data as ByteArray).size.toLong(),read)
        assertThat(file.length(),isEqualTo(numberOfBytes.toLong()))

        assertThat("read bytes and total bytes should be equal", read == total && read != -1L && total != -1L, isEqualTo(true))
        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpDownloadWithProgressValidCase() {
        val manager = FuelManager()

        val numberOfBytes = 45024
        val file = File.createTempFile(numberOfBytes.toString(), null)
        val bytes = ByteArray(numberOfBytes)
        Random().nextBytes(bytes)

        mockChain(
            request = mockRequest().withMethod(Method.GET.value).withPath("/bytes"),
            response = mockResponse().withBody(BinaryBody(bytes, MediaType.OCTET_STREAM))
        )

        var read = -1L
        var total = -1L

        val (request, response, result) = manager.download(mockPath("bytes")).destination { _, _ ->
            println(file.absolutePath)
            file
        }.progress { readBytes, totalBytes ->
            read = readBytes
            total = totalBytes
            println("read: $read, total: $total")
        }.responseString()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        assertThat(file.length(),isEqualTo(response.data.size.toLong()))
        assertThat(file.length(),isEqualTo(numberOfBytes.toLong()))

        assertThat("read bytes and total bytes should be equal", read == total && read != -1L && total != -1L, isEqualTo(true))
        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpDownloadWithProgressInvalidEndPointCase() {
        val manager = FuelManager()

        val numberOfBytes = 131072
        val file = File.createTempFile(numberOfBytes.toString(), null)

        mockChain(
            request = mockRequest().withMethod(Method.GET.value).withPath("/bytes"),
            response = mockResponse().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
        )

        val (request, response, result) = manager.download(mockPath("bytes")).destination { _, _ ->
            println(file.absolutePath)
            file
        }.progress { _, _ ->

        }.responseString()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, notNullValue())
        assertThat(data, nullValue())
        assertThat(file.length(),isEqualTo(0L))

        val statusCode = HttpURLConnection.HTTP_NOT_FOUND
        assertThat(response.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpDownloadWithProgressInvalidFileCase() {
        val manager = FuelManager()

        mockChain(
            request = mockRequest().withMethod(Method.GET.value).withPath("/bytes"),
            response = mockReflect()
        )

        val (request, response, result) = manager.download(mockPath("bytes")).destination { _, _ ->
            val dir = System.getProperty("user.dir")
            File.createTempFile("not_found_file", null, File(dir, "not-a-folder"))
        }.progress { _, _ ->

        }.responseString()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, notNullValue())
        assertThat(data, nullValue())

        val statusCode = -1
        assertThat(error?.exception as IOException, isA(IOException::class.java))
        assertThat(response.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpDownloadBigFileWithProgressValidCase() {
        val manager = FuelManager()

        val numberOfBytes = 1024 * 1024 // 1 MB
        val file = File.createTempFile(numberOfBytes.toString(), null)
        val bytes = ByteArray(numberOfBytes)
        Random().nextBytes(bytes)

        mockChain(
            request = mockRequest().withMethod(Method.GET.value).withPath("/bytes"),
            response = mockResponse()
                        .withDelay(TimeUnit.SECONDS, 3)
                        .withBody(BinaryBody(bytes, MediaType.OCTET_STREAM))
        )

        var read = -1L
        var total = -1L
        var lastPercent = 0L

        val (request, response, result) = manager.download(mockPath("bytes")).destination { _, _ ->
            println(file.absolutePath)
            file
        }.progress { readBytes, totalBytes ->
            read = readBytes
            total = totalBytes
            val percent = readBytes * 100 / totalBytes
            if (percent > lastPercent) {
                println("read: $read, total: $total, $percent% ")
                lastPercent = percent
            }
        }.responseString()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())
        assertThat(file.length(),isEqualTo(numberOfBytes.toLong()))

        assertThat("read bytes and total bytes should be equal", read == total && read != -1L && total != -1L, isEqualTo(true))
        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))
    }

}
