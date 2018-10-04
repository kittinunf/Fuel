package com.github.kittinunf.fuel.coroutines

import com.github.kittinunf.fuel.core.Deserializable
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Request.Companion.byteArrayDeserializer
import com.github.kittinunf.fuel.core.Request.Companion.stringDeserializer
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.core.response
import com.github.kittinunf.result.Result
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.suspendCancellableCoroutine
import kotlinx.coroutines.experimental.withContext
import java.nio.charset.Charset
import kotlin.coroutines.experimental.CoroutineContext

private suspend fun <T : Any, U : Deserializable<T>> Request.await(
        deserializable: U, scope: CoroutineContext
): Triple<Request, Response, Result<T, FuelError>> =
        withContext(scope) {
            suspendCancellableCoroutine<Triple<Request, Response, Result<T, FuelError>>> { continuation ->
                continuation.invokeOnCancellation { cancel() }
                continuation.resume(response(deserializable))
            }
        }

suspend fun Request.asyncByteArrayResponse(
        scope: CoroutineContext = CommonPool
): Deferred<Triple<Request, Response, Result<ByteArray, FuelError>>> =
        async {
            await(byteArrayDeserializer(), scope)
        }

suspend fun Request.asyncStringResponse(
        charset: Charset = Charsets.UTF_8,
        scope: CoroutineContext = CommonPool
): Deferred<Triple<Request, Response, Result<String, FuelError>>> =
        async {
            await(stringDeserializer(charset), scope)
        }

suspend fun <U : Any> Request.asyncObjectResponse(
        deserializable: ResponseDeserializable<U>,
        scope: CoroutineContext = CommonPool
): Deferred<Triple<Request, Response, Result<U, FuelError>>> =
        async {
            await(deserializable, scope)
        }

@Deprecated("please use 'asyncObjectResponse()'", ReplaceWith("asyncObjectResponse()"))
suspend fun <U : Any> Request.awaitObjectResponse(
        deserializable: ResponseDeserializable<U>,
        scope: CoroutineContext = CommonPool
): Triple<Request, Response, Result<U, FuelError>> = await(deserializable, scope)

/***
 * 
 *  @param scope : This is the coroutine context you want the call to be made on, the defaut is CommonPool
 *
 *  @return ByteArray if no exceptions are thrown
 */
@Deprecated("please use 'asyncByteArrayResponse()", ReplaceWith("asyncByteArrayResponse()"))
suspend fun Request.awaitByteArray(
        scope: CoroutineContext = CommonPool
): ByteArray = await(byteArrayDeserializer(), scope).third.get()

/**
 *  @note errors thrown in deserialization will not be caught
 *
 *  @param charset this is defaults to UTF-8
 *  @param scope : This is the coroutine context you want the call to be made on, the defaut is CommonPool
 *
 *  @return ByteArray if no exceptions are thrown
 */
@Deprecated("please use 'asyncStringResponse()", ReplaceWith("asyncStringResponse()"))
suspend fun Request.awaitString(
        charset: Charset = Charsets.UTF_8,
        scope: CoroutineContext = CommonPool
): String = await(stringDeserializer(charset), scope).third.get()

@Deprecated("please use 'asyncStringResponse()", ReplaceWith("asyncStringResponse()"))
suspend fun Request.awaitStringResponse(
        charset: Charset = Charsets.UTF_8,
        scope: CoroutineContext = CommonPool
): Triple<Request, Response, Result<String, FuelError>> = await(stringDeserializer(charset), scope)

/**
 * @note This function will throw the an exception if an error is thrown either at the HTTP level
 * or during deserialization
 *
 * @param deserializable
 * @param scope : This is the coroutine context you want the call to be made on, the defaut is CommonPool
 *
 * @return Result object
 */
@Deprecated("please use 'asyncObjectResponse()'", ReplaceWith("asyncObjectResponse()"))
suspend fun <U : Any> Request.awaitObject(
        deserializable: ResponseDeserializable<U>,
        scope: CoroutineContext = CommonPool
): U = await(deserializable, scope).third.get()

@Deprecated("please use 'asyncByteArrayResponse()", ReplaceWith("asyncByteArrayResponse()"))
suspend fun Request.awaitByteArrayResponse(
        scope: CoroutineContext = CommonPool
): Triple<Request, Response, Result<ByteArray, FuelError>> = await(byteArrayDeserializer(), scope)

/***
 * Response functions all these return a Result
 *
 * @param scope : This is the coroutine context you want the call to be made on, the defaut is CommonPool
 *
 * @return Result<ByteArray,FuelError>
 */
@Deprecated("please use 'asyncByteArrayResponse()", ReplaceWith("asyncByteArrayResponse()"))
suspend fun Request.awaitByteArrayResult(
        scope: CoroutineContext = CommonPool
): Result<ByteArray, FuelError> = awaitByteArrayResponse(scope).third

/**
 *
 * @param charset this is defaults to UTF-8
 * @param scope : This is the coroutine context you want the call to be made on, the defaut is CommonPool
 *
 * @return Result<String,FuelError>
 */
@Deprecated("please use 'asyncStringResponse()'", ReplaceWith("asyncStringResponse()"))
suspend fun Request.awaitStringResult(
        charset: Charset = Charsets.UTF_8,
        scope: CoroutineContext = CommonPool
): Result<String, FuelError> = awaitStringResponse(charset, scope).third

/**
 * This function catches both server errors and Deserialization Errors
 *
 * @param deserializable
 * @param scope : This is the coroutine context you want the call to be made on, the defaut is CommonPool
 *
 * @return Result object
 */
@Deprecated("please use 'asyncObjectResponse()'", ReplaceWith("asyncObjectResponse()"))
suspend fun <U : Any> Request.awaitObjectResult(
        deserializable: ResponseDeserializable<U>,
        scope: CoroutineContext = CommonPool
): Result<U, FuelError> = try {
    await(deserializable, scope).third
} catch (e: Exception) {
    val fuelError = when (e) {
        is FuelError -> e
        else -> FuelError(e)
    }
    Result.Failure(fuelError)
}
