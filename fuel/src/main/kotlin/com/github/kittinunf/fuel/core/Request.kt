package com.github.kittinunf.fuel.core

import com.github.kittinunf.fuel.core.deserializers.ByteArrayDeserializer
import com.github.kittinunf.fuel.core.deserializers.StringDeserializer
import com.github.kittinunf.fuel.core.requests.DownloadTaskRequest
import com.github.kittinunf.fuel.core.requests.TaskRequest
import com.github.kittinunf.fuel.core.requests.UploadTaskRequest
import com.github.kittinunf.fuel.util.Base64
import com.github.kittinunf.result.Result
import java.io.File
import java.net.URL
import java.nio.charset.Charset
import java.util.concurrent.Callable
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSocketFactory

class Request {
    enum class Type {
        REQUEST,
        DOWNLOAD,
        UPLOAD
    }

    enum class ContentType(val string: String) {
        Text(""),
        TextPlain("text/plain"),
        JSON("application/json"),
        Javascript("application/javascript"),
        XML("application/xml"),
        XMLText("text/xml"),
        HTML("text/html")
    }

    var timeoutInMillisecond = 15000

    var type: Type = Type.REQUEST
    lateinit var httpMethod: Method
    lateinit var path: String
    lateinit var url: URL
    var httpBody: ByteArray = ByteArray(0)

    var httpHeaders = hashMapOf<String, String>()

    //underlying task request
    val taskRequest: TaskRequest by lazy {
        when (type) {
            Type.DOWNLOAD -> DownloadTaskRequest(this)
            Type.UPLOAD -> UploadTaskRequest(this)
            else -> TaskRequest(this)
        }
    }

    var taskFuture: Future<*>? = null

    //configuration
    var socketFactory: SSLSocketFactory? = null
    var hostnameVerifier: HostnameVerifier? = null

    //callers
    lateinit var executor: ExecutorService
    lateinit var callbackExecutor: Executor

    //interfaces
    fun timeout(timeout: Int): Request {
        timeoutInMillisecond = timeout
        return this
    }

    fun header(vararg pairs: Pair<String, Any>?): Request {
        pairs.forEach {
            if (it != null)
                httpHeaders.plusAssign(Pair(it.first, it.second.toString()))
        }
        return this
    }

    fun header(pairs: Map<String, Any>?): Request = header(pairs, true)

    internal fun header(pairs: Map<String, Any>?, replace: Boolean): Request {
        pairs?.forEach {
            it.let {
                if (!httpHeaders.containsKey(it.key) || replace) {
                    httpHeaders.plusAssign(Pair(it.key, it.value.toString()))
                }
            }
        }
        return this
    }

    fun body(body: ByteArray): Request {
        httpBody = body
        return this
    }

    fun body(body: String, charset: Charset = Charsets.UTF_8): Request = body(body.toByteArray(charset))

    fun body(body: String, contentType: ContentType = ContentType.Text, charset: Charset = Charsets.UTF_8): Request {
        httpBody = body.toByteArray(charset)

        httpHeaders.remove("Content-Type")

        if (contentType != ContentType.Text) {
            header("Content-Type" to contentType.string)
        }

        return this
    }

    fun authenticate(username: String, password: String): Request {
        val auth = "$username:$password"
        val encodedAuth = Base64.encode(auth.toByteArray(), Base64.NO_WRAP)
        return header("Authorization" to "Basic " + String(encodedAuth))
    }

    fun validate(statusCodeRange: IntRange): Request {
        taskRequest.apply {
            validator = { response ->
                statusCodeRange.contains(response.httpStatusCode)
            }
        }
        return this
    }

    fun progress(handler: (readBytes: Long, totalBytes: Long) -> Unit): Request {
        if (taskRequest as? DownloadTaskRequest != null) {
            val download = taskRequest as DownloadTaskRequest
            download.apply {
                progressCallback = handler
            }
        } else if (taskRequest as? UploadTaskRequest != null) {
            val upload = taskRequest as UploadTaskRequest
            upload.apply {
                progressCallback = handler
            }
        } else {
            throw IllegalStateException("progress is only used with RequestType.DOWNLOAD or RequestType.UPLOAD")
        }

        return this
    }

    fun source(source: (Request, URL) -> File): Request {
        val uploadTaskRequest = taskRequest as? UploadTaskRequest ?: throw IllegalStateException("source is only used with RequestType.UPLOAD")

        uploadTaskRequest.apply {
            sourceCallback = source
        }
        return this
    }

    fun destination(destination: (Response, URL) -> File): Request {
        val downloadTaskRequest = taskRequest as? DownloadTaskRequest ?: throw IllegalStateException("destination is only used with RequestType.DOWNLOAD")

        downloadTaskRequest.apply {
            destinationCallback = destination
        }
        return this
    }

    fun interrupt(interrupt: (Request) -> Unit): Request {
        taskRequest.apply {
            interruptCallback = interrupt
        }
        return this
    }

    fun submit(callable: Callable<*>) {
        taskFuture = executor.submit(callable)
    }

    fun callback(f: () -> Unit) {
        callbackExecutor.execute {
            f.invoke()
        }
    }

    fun cancel() {
        taskFuture?.cancel(true)
    }


    fun cUrlString(): String {
        val elements = arrayListOf("$ curl -i")

        //method
        if (!httpMethod.equals(Method.GET)) {
            elements.add("-X $httpMethod")
        }

        //body
        val escapedBody = String(httpBody).replace("\"", "\\\"")
        if (escapedBody.isNotEmpty()) {
            elements.add("-d \"$escapedBody\"")
        }

        //headers
        for ((key, value) in httpHeaders) {
            elements.add("-H \"$key:$value\"")
        }

        //url
        elements.add("\"${url.toString()}\"")

        return elements.joinToString(" ").toString()
    }

    override fun toString(): String {
        val elements = arrayListOf("--> $httpMethod (${url.toString()})")

        //body
        elements.add("Body : ${ if (httpBody.size != 0) String(httpBody) else "(empty)"}")

        //headers
        elements.add("Headers : (${httpHeaders.size})")
        for ((key, value) in httpHeaders) {
            elements.add("$key : $value")
        }

        return elements.joinToString("\n").toString()
    }

    //byte array
    fun response(handler: (Request, Response, Result<ByteArray, FuelError>) -> Unit) =
            response(byteArrayDeserializer(), handler)

    fun response(handler: Handler<ByteArray>) = response(byteArrayDeserializer(), handler)

    fun response() = response(byteArrayDeserializer())

    //string
    fun responseString(charset: Charset = Charsets.UTF_8, handler: (Request, Response, Result<String, FuelError>) -> Unit) =
            response(stringDeserializer(charset), handler)

    fun responseString(charset: Charset, handler: Handler<String>) = response(stringDeserializer(charset), handler)
    fun responseString(handler: Handler<String>) = response(stringDeserializer(), handler)

    fun responseString(charset: Charset = Charsets.UTF_8) = response(stringDeserializer(charset))

    //object
    fun <T : Any> responseObject(deserializer: ResponseDeserializable<T>, handler: (Request, Response, Result<T, FuelError>) -> Unit) = response(deserializer, handler)

    fun <T : Any> responseObject(deserializer: ResponseDeserializable<T>, handler: Handler<T>) = response(deserializer, handler)

    fun <T : Any> responseObject(deserializer: ResponseDeserializable<T>) = response(deserializer)

    companion object {
        fun byteArrayDeserializer() = ByteArrayDeserializer

        fun stringDeserializer(charset: Charset = Charsets.UTF_8) = StringDeserializer(charset)
    }
}
