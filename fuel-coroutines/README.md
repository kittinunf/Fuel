
### Coroutines Support

Coroutines module provides extension functions to wrap a response inside a coroutine and handle its result. The coroutines-based API provides equivalent methods to the standard API (e.g: `responseString()` in coroutines is `awaitStringResponse()`).

```kotlin
runBlocking {
    val (request, response, result) = Fuel.get("https://httpbin.org/ip").awaitStringResponse()

    result.fold(
        { data -> println(data) /* "{"origin":"127.0.0.1"}" */ },
        { error -> println("An error of type ${error.exception} happened: ${error.message}") }
    )
}
```

There are functions to handle `Result` object directly too.

```kotlin
runBlocking {
    Fuel.get("https://httpbin.org/ip")
        .awaitStringResult()
        .fold(
            { data -> println(data) /* "{"origin":"127.0.0.1"}" */ },
            { error -> println("An error of type ${error.exception} happened: ${error.message}") }
        )
}
```

It also provides useful methods to retrieve the `ByteArray`,`String` or `Object` directly. The difference with these implementations is that they throw exception instead of returning it wrapped a `FuelError` instance.

```kotlin
runBlocking {
    try {
        println(Fuel.get("https://httpbin.org/ip").awaitString()) // "{"origin":"127.0.0.1"}"
    } catch(exception: Exception) {
        println("A network request exception was thrown: ${exception.message}")
    }
}
```

Handling objects other than `String` (`awaitStringResponse() `) or `ByteArray` (`awaitByteArrayResponse()`) can be done using `awaitObject`, `awaitObjectResult` or `awaitObjectResponse`.

```kotlin
data class Ip(val origin: String)

object IpDeserializer : ResponseDeserializable<Ip> {
    override fun deserialize(content: String) =
        jacksonObjectMapper().readValue<Ip>(content)
}
```

```kotlin
runBlocking {
    Fuel.get("https://httpbin.org/ip")
        .awaitObjectResult(IpDeserializer)
        .fold(
            { data -> println(data.origin) /* 127.0.0.1 */ },
            { error -> println("An error of type ${error.exception} happened: ${error.message}") }
        )
}
```

```kotlin
runBlocking {
    try {
        val data = Fuel.get("https://httpbin.org/ip").awaitObject(IpDeserializer)
        println(data.origin) // 127.0.0.1
    } catch (exception: Exception) {
        when (exception){
            is HttpException -> println("A network request exception was thrown: ${exception.message}")
            is JsonMappingException -> println("A serialization/deserialization exception was thrown: ${exception.message}")
            else -> println("An exception [${exception.javaClass.simpleName}\"] was thrown")
        }
    }
}
```