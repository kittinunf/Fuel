package com.github.kittinunf.fuel.core

import java.net.URL

class Response {

    lateinit var url: URL

    var httpStatusCode = -1
    var httpResponseMessage = ""
    var httpResponseHeaders = emptyMap<String, List<String>>()
    var httpContentLength = 0L

    //data
    var data = ByteArray(0)

    override fun toString(): String {
        val elements = mutableListOf("<-- $httpStatusCode ($url)")

        //response message
        elements.add("Response : $httpResponseMessage")

        //content length
        elements.add("Length : $httpContentLength")

        //body
        elements.add("Body : ${if (data.isNotEmpty()) String(data) else "(empty)"}")

        //headers
        //headers
        elements.add("Headers : (${httpResponseHeaders.size})")
        for ((key, value) in httpResponseHeaders) {
            elements.add("$key : $value")
        }

        return elements.joinToString("\n")
    }

}
