package com.github.kittinunf.fuel.core.requests

import com.github.kittinunf.fuel.RequestTest
import com.github.kittinunf.fuel.core.BlobDataPart
import com.github.kittinunf.fuel.core.FileDataPart
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.InlineDataPart
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.ResponseResultOf
import com.github.kittinunf.fuel.test.MockHttpTestCase
import com.github.kittinunf.fuel.test.MockReflected
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.endsWith
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.isA
import org.hamcrest.CoreMatchers.not
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.startsWith
import org.junit.Assert.assertThat
import org.junit.Assert.fail
import org.junit.Test
import java.io.File
import java.io.FileNotFoundException
import java.net.HttpURLConnection

class UploadRequestTest : MockHttpTestCase() {
    private val currentDir = File(System.getProperty("user.dir"), "src/test/assets")

    private fun assertFileUploaded(file: File, result: ResponseResultOf<MockReflected>, name: String? = file.nameWithoutExtension, fileName: String? = file.name): ResponseResultOf<MockReflected> {
        val (request, response, wrapped) = result
        val (data, error) = wrapped

        assertThat("Expected request not to be null", request, notNullValue())
        assertThat("Expected response not to be null", response, notNullValue())
        assertThat("Expected data, actual error $error", data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, equalTo(statusCode))

        // Assert the content-type
        assertThat(request[Headers.CONTENT_TYPE].lastOrNull(), startsWith("multipart/form-data"))
        assertThat(request[Headers.CONTENT_TYPE].lastOrNull(), containsString("boundary="))

        val body = data!!.body!!.string!!
        assertBodyFormat(body, request[Headers.CONTENT_TYPE].last().split("boundary=", limit = 2).last().trim('"'))

        val expectedContents = file.readText()
        assertThat(body, containsString(expectedContents))

        val contentDispositions = body.lines().filter { it.startsWith(Headers.CONTENT_DISPOSITION, true) }
        val contentDispositionParameters = contentDispositions.flatMap { it.split(";").map { it.trim() } }
        if (name != null) {
            val foundNames = contentDispositionParameters.filter { it.startsWith("name=") }
                .map { it.substringAfter("name=") }
                .map { it.trim('"') }

            assertThat("Expected $name to be the name, actual $foundNames", foundNames.contains(name), equalTo(true))
        }

        if (fileName != null) {
            val foundFileNames = contentDispositionParameters.filter { it.startsWith("filename=") }
                .map { it.substringAfter("filename=") }
                .map { it.trim('"') }

            assertThat("Expected $fileName to be the filename, actual $foundFileNames", foundFileNames.contains(fileName), equalTo(true))
        }

        return result
    }

    private fun assertBodyFormat(body: String, boundary: String) {
        val parts = body.split("--$boundary\r\n").toMutableList()
        if (parts.isEmpty()) {
            return
        }

        assertThat("Expected there to be at least one part, given there is at least one boundary", parts.size > 1, equalTo(true))
        assertThat("Expected body to start with boundary", parts.removeAt(0).isBlank(), equalTo(true))
        assertThat("Expected body to end with boundary EOF", parts.last(), endsWith("\r\n--$boundary--\r\n"))

        parts.forEach { part ->
            val lines = part.split("\r\n").toMutableList()
            val expected = mutableMapOf(Headers.CONTENT_DISPOSITION to false, Headers.CONTENT_TYPE to false, "\r\n" to false)
            val found = mutableMapOf(Headers.CONTENT_DISPOSITION to "", Headers.CONTENT_TYPE to "", "\r\n" to "")

            while (expected["\r\n"] == false && lines.isNotEmpty()) {
                val line = lines.removeAt(0)
                val match = expected.keys.find { line.startsWith(it, false) || (it == "\r\n" && line.isEmpty()) }

                if (match == null) {
                    println("Nothing found for $line")
                } else {

                    if (expected[match] == true) {
                        fail("Expected $match to be present at most once")
                    }

                    expected[match] = true
                    found[match] = line
                }
            }

            if (expected[Headers.CONTENT_DISPOSITION] == false) {
                fail("Expected ${Headers.CONTENT_DISPOSITION} to be present")
            }

            val contentDisposition = found[Headers.CONTENT_DISPOSITION]!!
            assertThat("Only form-data is allowed as content-disposition", contentDisposition, containsString(": form-data"))
            assertThat("Name parameter is required", contentDisposition, containsString("name="))
        }
    }

    @Test
    fun uploadFileAsDataPart() {
        val manager = FuelManager()

        mock.chain(
            request = mock.request().withMethod(Method.POST.value).withPath("/upload"),
            response = mock.reflect()
        )

        val file = File(currentDir, "lorem_ipsum_short.tmp")
        val triple = manager.upload(mock.path("upload"))
            .add(FileDataPart(file))
            .responseObject(MockReflected.Deserializer())

        assertFileUploaded(file, triple)
    }

    @Test
    fun uploadFileAndParameters() {
        val manager = FuelManager()

        mock.chain(
            request = mock.request().withMethod(Method.POST.value).withPath("/upload"),
            response = mock.reflect()
        )

        val file = File(currentDir, "lorem_ipsum_short.tmp")
        val triple = manager.upload(mock.path("upload"), parameters = listOf("foo" to "bar"))
            .add(FileDataPart(file, name = "file"))
            .responseObject(MockReflected.Deserializer())

        val (request, _, result) = assertFileUploaded(file, triple, name = "file")
        assertThat(request.url.toExternalForm(), not(containsString("foo")))

        val (data, _) = result
        assertThat(data!!.body!!.string, containsString("name=\"foo\""))
        assertThat(data.body!!.string, containsString("bar"))
    }

    @Test
    fun uploadFileWithRequestConvertible() {
        val manager = FuelManager()

        mock.chain(
            request = mock.request().withMethod(Method.GET.value).withPath("/upload"),
            response = mock.reflect()
        )

        val file = File(currentDir, "lorem_ipsum_long.tmp")
        val triple = manager.upload(RequestTest.RequestConvertibleImpl(Method.GET, mock.path("upload")))
            .add(FileDataPart(file))
            .responseObject(MockReflected.Deserializer())

        assertFileUploaded(file, triple)
    }

    @Test
    fun uploadFileUsingPut() {
        val manager = FuelManager()

        mock.chain(
            request = mock.request().withMethod(Method.PUT.value).withPath("/upload"),
            response = mock.reflect()
        )

        val file = File(currentDir, "lorem_ipsum_long.tmp")
        val triple = manager.upload(mock.path("upload"), Method.PUT)
            .add(FileDataPart(file))
            .responseObject(MockReflected.Deserializer())

        assertFileUploaded(file, triple)
    }

    @Test
    fun uploadFileUsingProgress() {
        val manager = FuelManager()

        mock.chain(
            request = mock.request().withMethod(Method.POST.value).withPath("/upload"),
            response = mock.reflect()
        )

        var read = -1L
        var total = -1L

        val file = File(currentDir, "lorem_ipsum_long.tmp")
        val triple = manager.upload(mock.path("upload"))
            .add(FileDataPart(file))
            .progress { readBytes, totalBytes -> read = readBytes; total = totalBytes }
            .responseObject(MockReflected.Deserializer())

        assertFileUploaded(file, triple)

        assertThat("Expected upload progress", read == total && read != -1L && total != -1L, equalTo(true))
    }

    @Test
    fun uploadToInvalidEndpoint() {
        val manager = FuelManager()

        mock.chain(
            request = mock.request().withMethod(Method.POST.value).withPath("/nope"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
        )

        val (request, response, result) = manager.upload(mock.path("nope"))
            .add(FileDataPart(File(currentDir, "lorem_ipsum_short.tmp")))
            .responseString()

        val (data, error) = result

        assertThat("Expected request not to be null", request, notNullValue())
        assertThat("Expected response not to be null", response, notNullValue())
        assertThat("Expected error, actual data $data", error, notNullValue())

        val statusCode = HttpURLConnection.HTTP_NOT_FOUND
        assertThat(response.statusCode, equalTo(statusCode))
    }

    @Test
    fun uploadNonExistingFile() {
        val manager = FuelManager()

        mock.chain(
            request = mock.request().withMethod(Method.POST.value).withPath("/upload"),
            response = mock.reflect()
        )

        val (request, response, result) = manager.upload(mock.path("upload"))
            .add { FileDataPart(File(currentDir, "not_found_file.tmp")) }
            .responseString()
        val (data, error) = result

        assertThat("Expected request not to be null", request, notNullValue())
        assertThat("Expected response not to be null", response, notNullValue())
        assertThat("Expected error, actual data $data", error, notNullValue())

        assertThat(error?.exception as FileNotFoundException, isA(FileNotFoundException::class.java))

        val statusCode = -1
        assertThat(response.statusCode, equalTo(statusCode))
    }

    @Test
    fun uploadMultipleFilesUnderSameField() {
        val manager = FuelManager()

        mock.chain(
            request = mock.request().withMethod(Method.POST.value).withPath("/upload"),
            response = mock.reflect()
        )

        val shortFile = File(currentDir, "lorem_ipsum_short.tmp")
        val longFile = File(currentDir, "lorem_ipsum_long.tmp")
        val triple = manager.upload(mock.path("upload"), parameters = listOf("foo" to "bar"))
            .add(
                FileDataPart(shortFile, name = "file"),
                FileDataPart(longFile, name = "file")
            )
            .responseObject(MockReflected.Deserializer())

        assertFileUploaded(shortFile, triple, name = "file")
        assertFileUploaded(longFile, triple, name = "file")

        assertThat(triple.third.component1()!!.body!!.string, not(containsString("multipart/mixed")))
    }

    @Test
    fun uploadMultipleFilesUnderSameFieldArray() {
        val manager = FuelManager()

        mock.chain(
                request = mock.request().withMethod(Method.POST.value).withPath("/upload"),
                response = mock.reflect()
        )

        val shortFile = File(currentDir, "lorem_ipsum_short.tmp")
        val longFile = File(currentDir, "lorem_ipsum_long.tmp")
        val triple = manager.upload(mock.path("upload"), parameters = listOf("foo" to "bar"))
            .add(
                FileDataPart(shortFile, name = "file[]"),
                FileDataPart(longFile, name = "file[]")
            )
            .responseObject(MockReflected.Deserializer())

        assertFileUploaded(shortFile, triple, name = "file[]")
        assertFileUploaded(longFile, triple, name = "file[]")

        assertThat(triple.third.component1()!!.body!!.string, not(containsString("multipart/mixed")))
    }

    @Test
    fun uploadMultipleFilesAsMultipleFields() {
        val manager = FuelManager()

        mock.chain(
            request = mock.request().withMethod(Method.POST.value).withPath("/upload"),
            response = mock.reflect()
        )

        val shortFile = File(currentDir, "lorem_ipsum_short.tmp")
        val longFile = File(currentDir, "lorem_ipsum_long.tmp")

        val triple = manager.upload(mock.path("upload"), parameters = listOf("foo" to "bar"))
            .add(FileDataPart(shortFile, contentType = "image/jpeg"))
            .add(FileDataPart(longFile, name = "second-file", contentType = "image/jpeg"))
            .responseObject(MockReflected.Deserializer())

        assertFileUploaded(shortFile, triple)
        assertFileUploaded(longFile, triple, name = "second-file")
    }

    @Test
    fun uploadBlob() {
        val file = File(currentDir, "lorem_ipsum_short.tmp")
        val blob = BlobDataPart(file.inputStream(), contentLength = file.length(), filename = file.name, name = "coolblob")
        val manager = FuelManager()

        mock.chain(
            request = mock.request().withMethod(Method.POST.value).withPath("/upload"),
            response = mock.reflect()
        )

        val triple = manager.upload(mock.path("upload"), parameters = listOf("foo" to "bar"))
            .add { blob }
            .responseObject(MockReflected.Deserializer())

        assertFileUploaded(file, triple, name = "coolblob", fileName = file.name)
    }

    @Test
    fun uploadWithCustomBoundary() {
        val manager = FuelManager()

        mock.chain(
            request = mock.request().withMethod(Method.POST.value).withPath("/upload"),
            response = mock.reflect()
        )

        val boundary = "160f77ec3eff"
        val file = File(currentDir, "lorem_ipsum_short.tmp")
        val triple = manager.upload(mock.path("upload"), parameters = listOf("foo" to "bar"))
            .add(FileDataPart(file))
            .header(Headers.CONTENT_TYPE, "multipart/form-data; boundary=\"$boundary\"")
            .responseObject(MockReflected.Deserializer())

        val (_, _, result) = assertFileUploaded(file, triple)
        val (data, _) = result

        val body = data!!.body!!.string
        assertThat(body, containsString("--$boundary--"))
    }

    @Test
    fun uploadWithInvalidBoundary() {
        val manager = FuelManager()

        mock.chain(
            request = mock.request().withMethod(Method.POST.value).withPath("/upload"),
            response = mock.reflect()
        )

        val (request, response, result) = manager.upload(mock.path("upload"), parameters = listOf("foo" to "bar"))
            .add(FileDataPart(File(currentDir, "lorem_ipsum_short.tmp")))
            .header(Headers.CONTENT_TYPE, "multipart/form-data")
            .responseObject(MockReflected.Deserializer())

        val (data, error) = result

        assertThat("Expected request not to be null", request, notNullValue())
        assertThat("Expected response not to be null", response, notNullValue())
        assertThat("Expected error, actual data $data", error, notNullValue())

        assertThat(error?.exception as? IllegalArgumentException, isA(IllegalArgumentException::class.java))
    }

    @Test
    fun uploadInlineDataPart() {
        val manager = FuelManager()
        mock.chain(
            request = mock.request().withMethod(Method.POST.value).withPath("/upload"),
            response = mock.reflect()
        )

        val shortFile = File(currentDir, "lorem_ipsum_short.tmp")
        val longFile = File(currentDir, "lorem_ipsum_long.tmp")
        val metadata = longFile.readText()
        val triple = manager.upload(mock.path("upload"), parameters = listOf("foo" to "bar"))
            .add(
                FileDataPart(shortFile, name = "file"),
                InlineDataPart(metadata, name = "metadata", contentType = "application/json", filename = "metadata.json")
            )
            .responseObject(MockReflected.Deserializer())

        assertFileUploaded(shortFile, triple, name = "file")
        assertFileUploaded(longFile, triple, name = "metadata", fileName = "metadata.json")

        assertThat(triple.third.component1()!!.body!!.string, not(containsString("multipart/mixed")))
    }
}
