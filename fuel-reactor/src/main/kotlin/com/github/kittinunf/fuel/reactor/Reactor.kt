package com.github.kittinunf.fuel.reactor

import com.github.kittinunf.fuel.core.Deserializable
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.deserializers.ByteArrayDeserializer
import com.github.kittinunf.fuel.core.deserializers.StringDeserializer
import com.github.kittinunf.fuel.core.requests.CancellableRequest
import com.github.kittinunf.fuel.core.response
import com.github.kittinunf.result.Result
import reactor.core.publisher.Mono
import reactor.core.publisher.MonoSink
import java.nio.charset.Charset

private fun <T : Any> Request.monoResult(async: Request.(MonoSink<T>) -> CancellableRequest): Mono<T> =
    Mono.create<T> { sink ->
        val cancellableRequest = async(sink)
        sink.onCancel { cancellableRequest.cancel() }
    }

private fun <T : Any> Request.monoResultFold(mapper: Deserializable<T>): Mono<T> =
    monoResult { sink ->
        response(mapper) { _, _, result ->
            result.fold(sink::success, sink::error)
        }
    }

private fun <T : Any> Request.monoResultUnFolded(mapper: Deserializable<T>): Mono<Result<T, FuelError>> =
    monoResult { sink ->
        response(mapper) { _, _, result ->
            sink.success(result)
        }
    }

/**
 * Get a single [Response]
 * @return [Mono<Response>] the [Mono]
 */
fun Request.monoResponse(): Mono<Response> =
    monoResult { sink ->
        response { _, res, _ -> sink.success(res) }
    }

/**
 * Get a single [ByteArray] via a [MonoSink.success], or any [FuelError] via [MonoSink.error]
 *
 * @see monoResultBytes
 * @return [Mono<ByteArray>] the [Mono]
 */
fun Request.monoBytes(): Mono<ByteArray> = monoResultFold(ByteArrayDeserializer())

/**
 * Get a single [String] via a [MonoSink.success], or any [FuelError] via [MonoSink.error]
 *
 * @see monoResultString
 *
 * @param charset [Charset] the charset to use for the string, defaults to [Charsets.UTF_8]
 * @return [Mono<String>] the [Mono]
 */
fun Request.monoString(charset: Charset = Charsets.UTF_8): Mono<String> = monoResultFold(StringDeserializer(charset))

/**
 * Get a single [T] via a [MonoSink.success], or any [FuelError] via [MonoSink.error]
 *
 * @see monoResultObject
 *
 * @param mapper [Deserializable<T>] the deserializable that can turn the response int a [T]
 * @return [Mono<T>] the [Mono]
 */
fun <T : Any> Request.monoObject(mapper: Deserializable<T>): Mono<T> = monoResultFold(mapper)

/**
 * Get a single [ByteArray] or [FuelError] via [Result]
 *
 * @see monoBytes
 * @return [Mono<Result<ByteArray, FuelError>>] the [Mono]
 */
fun Request.monoResultBytes(): Mono<Result<ByteArray, FuelError>> =
    monoResultUnFolded(ByteArrayDeserializer())

/**
 * Get a single [String] or [FuelError] via [Result]
 *
 * @see monoString
 *
 * @param charset [Charset] the charset to use for the string, defaults to [Charsets.UTF_8]
 * @return [Mono<Result<ByteArray, FuelError>>] the [Mono]
 */
fun Request.monoResultString(charset: Charset = Charsets.UTF_8): Mono<Result<String, FuelError>> =
    monoResultUnFolded(StringDeserializer(charset))

/**
 * Get a single [T] or [FuelError] via [Result]
 *
 * @see monoObject
 * @return [Mono<Result<T, FuelError>>] the [Mono]
 */
fun <T : Any> Request.monoResultObject(mapper: Deserializable<T>): Mono<Result<T, FuelError>> =
    monoResultUnFolded(mapper)