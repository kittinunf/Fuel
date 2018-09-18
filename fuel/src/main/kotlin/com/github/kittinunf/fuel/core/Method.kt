package com.github.kittinunf.fuel.core

/**
 * HTTP method as defined by RFC 7231 and 5489
 *
 * @see https://tools.ietf.org/html/rfc7231
 * @see https://tools.ietf.org/html/rfc5789
 *
 * CONNECT is not included as it's not supported by the java jvm (java.net.ProtocolException).
 */
enum class Method(val value: String) {
    GET("GET"),
    HEAD("HEAD"),
    POST("POST"),
    PUT("PUT"),
    DELETE("DELETE"),
    OPTIONS("OPTIONS"),
    TRACE("TRACE"),
    PATCH("PATCH"),
}

