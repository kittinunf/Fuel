package com.github.kittinunf.fuel.moshi

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Handler
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.core.response
import com.github.kittinunf.result.Result
import com.squareup.moshi.Moshi
import com.squareup.moshi.JsonAdapter

inline fun <reified T : Any> Request.responseObject(noinline handler: (Request, Response, Result<T, FuelError>) -> Unit) =
        response(moshiDeserializerOf(T::class.java), handler)

inline fun <reified T : Any> Request.responseObject(handler: Handler<T>) = response(moshiDeserializerOf(T::class.java), handler)

inline fun <reified T : Any> Request.responseObject() = response(moshiDeserializerOf(T::class.java))

fun <T : Any> moshiDeserializerOf(clazz: Class<T>) = object : ResponseDeserializable<T> {
    override fun deserialize(content: String): T? = Moshi.Builder()
                .build()
                .adapter(clazz)
                .fromJson(content)
}

inline fun <reified T : Any> moshiDeserializerOf(adapter: JsonAdapter<T>) = object : ResponseDeserializable<T> {
    override fun deserialize(content: String): T? =
            adapter.fromJson(content)
}
