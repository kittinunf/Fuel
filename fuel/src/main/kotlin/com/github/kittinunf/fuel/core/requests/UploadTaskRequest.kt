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
import javax.activation.MimetypesFileTypeMap

class UploadTaskRequest(request: Request) : TaskRequest(request) {

    val BUFFER_SIZE = 1024

    val CRLF = "\r\n"
    val boundary = request.httpHeaders["Content-Type"]?.split("=", limit = 2)?.get(1) ?: System.currentTimeMillis().toHexString()

    var progressCallback: ((Long, Long) -> Unit)? = null
    lateinit var sourceCallback: (Request, URL) -> Iterable<File>

    var dataStream: ByteArrayOutputStream? = null

    override fun call(): Response {
        try {
            dataStream = ByteArrayOutputStream().apply {
                val files = sourceCallback.invoke(request, request.url)

                files.forEachIndexed { i, file ->
                    val postFix = if (files.count() == 1) "" else "${i + 1}"
                    val fileName = request.names.getOrElse(i) { request.name + postFix }

                    write("--$boundary")
                    write(CRLF)
                    write("Content-Disposition: form-data; name=\"$fileName\"; filename=\"${file.name}\"")
                    write(CRLF)
                    write("Content-Type: " + request.mediaTypes.getOrElse(i) { guessContentType(file) })
                    write(CRLF)
                    write(CRLF)

                    //input file data
                    FileInputStream(file).use {
                        it.copyTo(this, BUFFER_SIZE) { writtenBytes ->
                            progressCallback?.invoke(writtenBytes, file.length())
                        }
                    }

                    write(CRLF)
                }

                request.parameters.forEach {
                    write("--$boundary")
                    write(CRLF)
                    write("Content-Disposition: form-data; name=\"${it.first}\"")
                    write(CRLF)
                    write("Content-Type: text/plain")
                    write(CRLF)
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
        }
    }

    private fun guessContentType(file: File): String {
        return URLConnection.guessContentTypeFromName(file.name) ?: MimetypesFileTypeMap().getContentType(file)
    }
}

fun OutputStream.write(str: String) = write(str.toByteArray())
