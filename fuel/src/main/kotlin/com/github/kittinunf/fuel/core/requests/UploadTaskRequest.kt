package com.github.kittinunf.fuel.core.requests

import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.util.copyTo
import com.github.kittinunf.fuel.util.toHexString
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.OutputStream
import java.net.URL
import java.net.URLConnection

class UploadTaskRequest(request: Request) : TaskRequest(request) {

    val BUFFER_SIZE = 1024

    val CRLF = "\r\n"
    val boundary = request.httpHeaders["Content-Type"]?.split("=", limit = 2)?.get(1) ?: System.currentTimeMillis().toHexString()

    var progressCallback: ((Long, Long) -> Unit)? = null
    lateinit var sourceCallback: ((Request, URL) -> File)

    var dataStream: ByteArrayOutputStream? = null
    var fileInputStream: FileInputStream? = null

    override fun call(): Response {
        try {
            val file = sourceCallback.invoke(request, request.url)
            //file input
            fileInputStream = FileInputStream(file)
            dataStream = ByteArrayOutputStream().apply {
                write("--" + boundary + CRLF)
                write("Content-Disposition: form-data; name=\"" + request.name + "\"; filename=\"" + file.name + "\"")
                write(CRLF)
                write("Content-Type: " + URLConnection.guessContentTypeFromName(file.name))
                write(CRLF)
                write(CRLF)

                //input file data
                fileInputStream!!.copyTo(this, BUFFER_SIZE) { writtenBytes ->
                    progressCallback?.invoke(writtenBytes, file.length())
                }

                write(CRLF)

                request.parameters.forEach {
                    write("--$boundary" + CRLF)
                    write("Content-Disposition: form-data; name=\"" + it.first + "\"" + CRLF)
                    write("Content-Type: text/plain" + CRLF)
                    write(CRLF)
                    write(it.second.toString())
                    write(CRLF)
                }

                write(("--$boundary--"))
                write(CRLF)
                flush()
            }

            request.body(dataStream!!.toByteArray())
            return super.call()
        } finally {
            dataStream?.close()
            fileInputStream?.close()
        }
    }
}

fun OutputStream.write(str: String) = write(str.toByteArray())
