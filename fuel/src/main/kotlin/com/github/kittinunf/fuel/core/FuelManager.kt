package com.github.kittinunf.fuel.core

import com.github.kittinunf.fuel.core.Client.Hook
import com.github.kittinunf.fuel.core.RequestFactory.PathStringConvertible
import com.github.kittinunf.fuel.core.RequestFactory.RequestConvertible
import com.github.kittinunf.fuel.core.interceptors.ParameterEncoder
import com.github.kittinunf.fuel.core.interceptors.redirectResponseInterceptor
import com.github.kittinunf.fuel.core.requests.DownloadRequest
import com.github.kittinunf.fuel.core.requests.UploadRequest
import com.github.kittinunf.fuel.core.requests.download
import com.github.kittinunf.fuel.core.requests.upload
import com.github.kittinunf.fuel.toolbox.HttpClient
import com.github.kittinunf.fuel.util.readWriteLazy
import java.net.Proxy
import java.security.KeyStore
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory

typealias FoldableRequestInterceptor = (RequestTransformer) -> RequestTransformer
typealias FoldableResponseInterceptor = (ResponseTransformer) -> ResponseTransformer

class FuelManager : RequestFactory, RequestFactory.Convenience {

    var client: Client by readWriteLazy { HttpClient(proxy, hook = hook) }
    var proxy: Proxy? = null
    var basePath: String? = null
    var timeoutInMillisecond: Int = 15_000
    var timeoutReadInMillisecond: Int = timeoutInMillisecond
    var progressBufferSize: Int = DEFAULT_BUFFER_SIZE
    var hook: Hook = DefaultHook()

    var baseHeaders: Map<String, String>? = null
    var baseParams: Parameters = emptyList()

    var keystore: KeyStore? = null
    var socketFactory: SSLSocketFactory by readWriteLazy {
        keystore?.let {
            val trustFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            trustFactory.init(it)
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustFactory.trustManagers, null)
            sslContext.socketFactory
        } ?: HttpsURLConnection.getDefaultSSLSocketFactory()
    }

    var hostnameVerifier: HostnameVerifier by readWriteLazy {
        HttpsURLConnection.getDefaultHostnameVerifier()
    }

    // background executionOptions
    var executorService: ExecutorService by readWriteLazy {
        Executors.newCachedThreadPool { command ->
            Thread(command).also { thread ->
                thread.priority = Thread.NORM_PRIORITY
                thread.isDaemon = true
            }
        }
    }

    private val requestInterceptors: MutableList<FoldableRequestInterceptor> =
            mutableListOf(ParameterEncoder)
    private val responseInterceptors: MutableList<FoldableResponseInterceptor> =
            mutableListOf(redirectResponseInterceptor(this))

    // callback executionOptions
    var callbackExecutor: Executor by readWriteLazy { createEnvironment().callbackExecutor }

    var forceMethods: Boolean = false
    /**
     * Make a request using [method] to [path] with [parameters]
     *
     * @see FuelManager.instance
     * @see FuelManager.applyOptions
     *
     * @param method [Method] the HTTP method to make the request with
     * @param path [String] the absolute url or relative to [FuelManager.instance] basePath
     * @param parameters [Parameters?] list of parameters
     *
     * @return [Request] the request
     */
    override fun request(method: Method, path: String, parameters: Parameters?): Request {
        val request = request(Encoding(
            httpMethod = method,
            urlString = path,
            baseUrlString = basePath,
            parameters = if (parameters == null) baseParams else baseParams + parameters
        ).request)
        return applyOptions(request)
    }

    /**
     * Make a request using [method] to [convertible]'s path with [parameters]
     *
     * @see FuelManager.instance
     * @see RequestFactory(Method, String, Parameters?)
     *
     * @param method [Method] the HTTP method to make the request with
     * @param convertible [PathStringConvertible]
     * @param parameters [Parameters?] list of parameters
     *
     * @return [Request] the request
     */
    override fun request(method: Method, convertible: PathStringConvertible, parameters: Parameters?): Request =
        request(method, convertible.path, parameters)

    /**
     * Make a request using from [convertible]
     *
     * @param convertible [RequestConvertible] the instance that can be turned into a [Request]
     * @return [Request] the request
     */
    override fun request(convertible: RequestConvertible): Request = applyOptions(convertible.request)

    /**
     * Create a [method] [Request] to [path] with [parameters], which can download to a file
     *
     * @param path [String] the absolute or relative to [FuelManager.instance]' base-path path
     * @param method [Method] the method to download with, defaults to [Method.GET]
     * @param parameters [Parameters] the optional parameters
     * @return [DownloadRequest] the request (extended for download)
     */
    override fun download(path: String, method: Method, parameters: Parameters?): DownloadRequest {
        val request = Encoding(
            httpMethod = method,
            urlString = path,
            baseUrlString = basePath,
            parameters = if (parameters == null) baseParams else baseParams + parameters
        ).request
        return applyOptions(request).download()
    }

    /**
     * Create a [method] [Request] to [path] with [parameters], which can upload blobs and Data Parts
     *
     * @param path [String] the absolute or relative to [FuelManager.instance]' base-path path
     * @param method [Method] the method to upload with, defaults to [Method.POST]
     * @param parameters [Parameters] the optional parameters
     * @return [UploadRequest] the request (extended for upload)
     */
    override fun upload(path: String, method: Method, parameters: Parameters?): UploadRequest {
        val request = Encoding(
            httpMethod = method,
            urlString = path,
            baseUrlString = basePath,
            parameters = if (parameters == null) baseParams else baseParams + parameters
        ).request
        return applyOptions(request).upload()
    }

    /**
     * Make an upload request using from [convertible]
     *
     * @param convertible [RequestConvertible] the instance that can be turned into a [Request]
     * @return [UploadRequest] the request (extended for upload)
     */
    override fun upload(convertible: RequestConvertible): UploadRequest {
        return applyOptions(convertible.request).upload()
    }

    fun addRequestInterceptor(interceptor: FoldableRequestInterceptor): FuelManager {
        requestInterceptors += interceptor
        return this
    }

    fun addResponseInterceptor(interceptor: FoldableResponseInterceptor): FuelManager {
        responseInterceptors += interceptor
        return this
    }

    fun removeRequestInterceptor(interceptor: FoldableRequestInterceptor): FuelManager {
        requestInterceptors -= interceptor
        return this
    }

    fun removeResponseInterceptor(interceptor: FoldableResponseInterceptor): FuelManager {
        responseInterceptors -= interceptor
        return this
    }

    fun removeAllRequestInterceptors(): FuelManager {
        requestInterceptors.clear()
        return this
    }

    fun removeAllResponseInterceptors(): FuelManager {
        responseInterceptors.clear()
        return this
    }

    private fun applyOptions(request: Request): Request {
        // Sets base headers ONLY if they are not set
        val unsetBaseHeaders = request.headers.keys.fold(Headers.from(baseHeaders.orEmpty())) {
            result, it -> result.remove(it); result
        }

        return request.header(unsetBaseHeaders).apply {
            executionOptions = RequestExecutionOptions(
                client = client,
                socketFactory = socketFactory,
                hostnameVerifier = hostnameVerifier,
                callbackExecutor = callbackExecutor,
                requestTransformer = requestInterceptors.foldRight({ r: Request -> r }) { f, acc -> f(acc) },
                responseTransformer = responseInterceptors.foldRight({ _: Request, res: Response -> res }) { f, acc -> f(acc) },
                executorService = executorService
            ).also { executor ->
                executor.timeoutInMillisecond = timeoutInMillisecond
                executor.timeoutReadInMillisecond = timeoutReadInMillisecond
                executor.forceMethods = forceMethods
            }
        }
    }

    companion object {
        // manager
        var instance by readWriteLazy { FuelManager() }
        val progressBufferSize: Int get() = instance.progressBufferSize
    }

    /**
     * Create a [Method.GET] [Request] to [path] with [parameters]
     *
     * @param path [String] the absolute or relative to [FuelManager.instance]' base-path path
     * @param parameters [Parameters] the optional parameters
     * @return [Request] the request
     */
    override fun get(path: String, parameters: Parameters?): Request =
        request(Method.GET, path, parameters)

    /**
     * Create a [Method.GET] [Request] to [PathStringConvertible.path] with [parameters]
     *
     * @param convertible [PathStringConvertible] the absolute or relative to [FuelManager.instance]' base-path path
     * @param parameters [Parameters] the optional parameters
     * @return [Request] the request
     */
    override fun get(convertible: PathStringConvertible, parameters: Parameters?): Request =
        request(Method.GET, convertible, parameters)

    /**
     * Create a [Method.POST] [Request] to [path] with [parameters]
     *
     * @param path [String] the absolute or relative to [FuelManager.instance]' base-path path
     * @param parameters [Parameters] the optional parameters
     * @return [Request] the request
     */
    override fun post(path: String, parameters: Parameters?): Request =
        request(Method.POST, path, parameters)

    /**
     * Create a [Method.POST] [Request] to [PathStringConvertible.path] with [parameters]
     *
     * @param convertible [PathStringConvertible] the absolute or relative to [FuelManager.instance]' base-path path
     * @param parameters [Parameters] the optional parameters
     * @return [Request] the request
     */
    override fun post(convertible: PathStringConvertible, parameters: Parameters?): Request =
        request(Method.POST, convertible, parameters)

    /**
     * Create a [Method.PUT] [Request] to [path] with [parameters]
     *
     * @param path [String] the absolute or relative to [FuelManager.instance]' base-path path
     * @param parameters [Parameters] the optional parameters
     * @return [Request] the request
     */
    override fun put(path: String, parameters: Parameters?): Request =
        request(Method.PUT, path, parameters)

    /**
     * Create a [Method.PUT] [Request] to [PathStringConvertible.path] with [parameters]
     *
     * @param convertible [PathStringConvertible] the absolute or relative to [FuelManager.instance]' base-path path
     * @param parameters [Parameters] the optional parameters
     * @return [Request] the request
     */
    override fun put(convertible: PathStringConvertible, parameters: Parameters?): Request =
        request(Method.PUT, convertible, parameters)

    /**
     * Create a [Method.PATCH] [Request] to [path] with [parameters]
     *
     * @param path [String] the absolute or relative to [FuelManager.instance]' base-path path
     * @param parameters [Parameters] the optional parameters
     * @return [Request] the request
     */
    override fun patch(path: String, parameters: Parameters?): Request =
        request(Method.PATCH, path, parameters)

    /**
     * Create a [Method.PATCH] [Request] to [PathStringConvertible.path] with [parameters]
     *
     * @param convertible [PathStringConvertible] the absolute or relative to [FuelManager.instance]' base-path path
     * @param parameters [Parameters] the optional parameters
     * @return [Request] the request
     */
    override fun patch(convertible: PathStringConvertible, parameters: Parameters?): Request =
        request(Method.PATCH, convertible, parameters)

    /**
     * Create a [Method.DELETE] [Request] to [path] with [parameters]
     *
     * @param path [String] the absolute or relative to [FuelManager.instance]' base-path path
     * @param parameters [Parameters] the optional parameters
     * @return [Request] the request
     */
    override fun delete(path: String, parameters: Parameters?): Request =
        request(Method.DELETE, path, parameters)

    /**
     * Create a [Method.DELETE] [Request] to [PathStringConvertible.path] with [parameters]
     *
     * @param convertible [PathStringConvertible] the absolute or relative to [FuelManager.instance]' base-path path
     * @param parameters [Parameters] the optional parameters
     * @return [Request] the request
     */
    override fun delete(convertible: PathStringConvertible, parameters: Parameters?): Request =
        request(Method.DELETE, convertible, parameters)

    /**
     * Create a [method] [Request] to [PathStringConvertible.path] with [parameters], which can download to a file
     *
     * @param convertible [PathStringConvertible] the absolute or relative to [FuelManager.instance]' base-path path
     * @param method [Method] the method to download with, defaults to [Method.GET]
     * @param parameters [Parameters] the optional parameters
     * @return [DownloadRequest] the request (extended for download)
     */
    override fun download(convertible: PathStringConvertible, method: Method, parameters: Parameters?): DownloadRequest =
        download(convertible.path, method, parameters)

    /**
     * Create a [method] [Request] to [PathStringConvertible.path] with [parameters], which can upload blobs and
     * Data Parts
     *
     * @param convertible [PathStringConvertible] the absolute or relative to [FuelManager.instance]' base-path path
     * @param method [Method] the method to upload with, defaults to [Method.POST]
     * @param parameters [Parameters] the optional parameters
     * @return [UploadRequest] the request (extended for upload)
     */
    override fun upload(convertible: PathStringConvertible, method: Method, parameters: Parameters?): UploadRequest =
        upload(convertible.path, method, parameters)

    /**
     * Create a [Method.HEAD] [Request] to [path] with [parameters]
     *
     * @param path [String] the absolute or relative to [FuelManager.instance]' base-path path
     * @param parameters [Parameters] the optional parameters
     * @return [Request] the request
     */
    override fun head(path: String, parameters: Parameters?): Request =
        request(Method.HEAD, path, parameters)

    /**
     * Create a [Method.HEAD] [Request] to [PathStringConvertible.path] with [parameters]
     *
     * @param convertible [PathStringConvertible] the absolute or relative to [FuelManager.instance]' base-path path
     * @param parameters [Parameters] the optional parameters
     * @return [Request] the request
     */
    override fun head(convertible: PathStringConvertible, parameters: Parameters?): Request =
        request(Method.HEAD, convertible, parameters)

    /**
     * Resets this FuelManager to a clean instance
     */
    fun reset(): FuelManager {
        val clean = FuelManager()

        client = clean.client
        proxy = clean.proxy
        basePath = clean.basePath
        timeoutInMillisecond = clean.timeoutInMillisecond
        timeoutReadInMillisecond = clean.timeoutReadInMillisecond
        baseHeaders = clean.baseHeaders
        baseParams = clean.baseParams
        keystore = clean.keystore
        socketFactory = clean.socketFactory
        hostnameVerifier = clean.hostnameVerifier
        executorService = clean.executorService
        requestInterceptors.apply {
            clear()
            addAll(clean.requestInterceptors)
        }
        responseInterceptors.apply {
            clear()
            addAll(clean.responseInterceptors)
        }
        callbackExecutor = clean.callbackExecutor

        return this
    }
}
