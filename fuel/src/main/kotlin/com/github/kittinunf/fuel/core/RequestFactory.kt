package com.github.kittinunf.fuel.core

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.requests.DownloadRequest
import com.github.kittinunf.fuel.core.requests.UploadRequest

interface RequestFactory {

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
    fun request(method: Method, convertible: PathStringConvertible, parameters: Parameters? = null): Request

    /**
     * Make a request using [method] to [path] with [parameters]
     *
     * @see FuelManager.instance
     * @see FuelManager.request
     *
     * @param method [Method] the HTTP method to make the request with
     * @param path [String] the absolute url or relative to [FuelManager.instance] basePath
     * @param parameters [Parameters?] list of parameters
     *
     * @return [Request] the request
     */
    fun request(method: Method, path: String, parameters: Parameters? = null): Request

    /**
     * Make a request using from [convertible]
     *
     * @param convertible [RequestConvertible] the instance that can be turned into a [Request]
     * @return [Request] the request
     */
    fun request(convertible: RequestConvertible): Request

    /**
     * Anything that is a [PathStringConvertible] can be used as [path] parameter with [Fuel]
     */
    interface PathStringConvertible {
        val path: String
    }

    /**
     * Anything that is [RequestConvertible] can be used as [request] with [Fuel.request]
     */
    interface RequestConvertible {
        val request: Request
    }

    interface Convenience : RequestFactory {
        /**
         * Create a [method] [Request] to [path] with [parameters], which can download to a file
         *
         * @parameters path [String] the absolute or relative to [FuelManager.instance]' base-path path
         * @parameters method [Method] the method to download with, defaults to [Method.GET]
         * @parameters parameters [Parameters] the optional parameters
         * @return [DownloadRequest] the request (extended for download)
         */
        fun download(path: String, method: Method = Method.GET, parameters: Parameters? = null): DownloadRequest

        /**
         * Create a [method] [Request] to [PathStringConvertible.path] with [parameters], which can download to a file
         *
         * @param convertible [PathStringConvertible] the absolute or relative to [FuelManager.instance]' base-path path
         * @param method [Method] the method to download with, defaults to [Method.GET]
         * @param parameters [Parameters] the optional parameters
         * @return [DownloadRequest] the request (extended for download)
         */
        fun download(convertible: PathStringConvertible, method: Method = Method.GET, parameters: Parameters? = null): DownloadRequest

        /**
         * Create a [method] [Request] to [PathStringConvertible.path] with [parameters], which can upload blobs and
         * Data Parts
         *
         * @param convertible [PathStringConvertible] the absolute or relative to [FuelManager.instance]' base-path path
         * @param method [Method] the method to upload with, defaults to [Method.POST]
         * @param parameters [Parameters] the optional parameters
         * @return [UploadRequest] the request (extended for upload)
         */
        fun upload(convertible: PathStringConvertible, method: Method = Method.POST, parameters: Parameters? = null): UploadRequest

        /**
         * Create a [method] [Request] to [path] with [parameters], which can upload blobs and Data Parts
         *
         * @parameters path [String] the absolute or relative to [FuelManager.instance]' base-path path
         * @parameters method [Method] the method to upload with, defaults to [Method.POST]
         * @parameters parameters [Parameters] the optional parameters
         * @return [UploadRequest] the request (extended for upload)
         */
        fun upload(path: String, method: Method = Method.POST, parameters: Parameters? = null): UploadRequest

        /**
         * Make an upload request using from [convertible]
         *
         * @param convertible [RequestConvertible] the instance that can be turned into a [Request]
         * @return [UploadRequest] the request (extended for upload)
         */
        fun upload(convertible: RequestConvertible): UploadRequest

            /**
         * Create a [Method.GET] [Request] to [path] with [parameters]
         *
         * @param path [String] the absolute or relative to [FuelManager.instance]' base-path path
         * @param parameters [Parameters] the optional parameters
         * @return [Request] the request
         */
        fun get(path: String, parameters: Parameters? = null): Request

        /**
         * Create a [Method.GET] [Request] to [PathStringConvertible.path] with [parameters]
         *
         * @param convertible [PathStringConvertible] the absolute or relative to [FuelManager.instance]' base-path path
         * @param parameters [Parameters] the optional parameters
         * @return [Request] the request
         */
        fun get(convertible: PathStringConvertible, parameters: Parameters? = null): Request

        /**
         * Create a [Method.POST] [Request] to [path] with [parameters]
         *
         * @param path [String] the absolute or relative to [FuelManager.instance]' base-path path
         * @param parameters [Parameters] the optional parameters
         * @return [Request] the request
         */
        fun post(path: String, parameters: Parameters? = null): Request

        /**
         * Create a [Method.POST] [Request] to [PathStringConvertible.path] with [parameters]
         *
         * @param convertible [PathStringConvertible] the absolute or relative to [FuelManager.instance]' base-path path
         * @param parameters [Parameters] the optional parameters
         * @return [Request] the request
         */
        fun post(convertible: PathStringConvertible, parameters: Parameters? = null): Request

        /**
         * Create a [Method.PUT] [Request] to [path] with [parameters]
         *
         * @param path [String] the absolute or relative to [FuelManager.instance]' base-path path
         * @param parameters [Parameters] the optional parameters
         * @return [Request] the request
         */
        fun put(path: String, parameters: Parameters? = null): Request

        /**
         * Create a [Method.PUT] [Request] to [PathStringConvertible.path] with [parameters]
         *
         * @param convertible [PathStringConvertible] the absolute or relative to [FuelManager.instance]' base-path path
         * @param parameters [Parameters] the optional parameters
         * @return [Request] the request
         */
        fun put(convertible: PathStringConvertible, parameters: Parameters? = null): Request

        /**
         * Create a [Method.PATCH] [Request] to [path] with [parameters]
         *
         * @param path [String] the absolute or relative to [FuelManager.instance]' base-path path
         * @param parameters [Parameters] the optional parameters
         * @return [Request] the request
         */
        fun patch(path: String, parameters: Parameters? = null): Request

        /**
         * Create a [Method.PATCH] [Request] to [PathStringConvertible.path] with [parameters]
         *
         * @param convertible [PathStringConvertible] the absolute or relative to [FuelManager.instance]' base-path path
         * @param parameters [Parameters] the optional parameters
         * @return [Request] the request
         */
        fun patch(convertible: PathStringConvertible, parameters: Parameters? = null): Request

        /**
         * Create a [Method.DELETE] [Request] to [path] with [parameters]
         *
         * @param path [String] the absolute or relative to [FuelManager.instance]' base-path path
         * @param parameters [Parameters] the optional parameters
         * @return [Request] the request
         */
        fun delete(path: String, parameters: Parameters? = null): Request

        /**
         * Create a [Method.DELETE] [Request] to [PathStringConvertible.path] with [parameters]
         *
         * @param convertible [PathStringConvertible] the absolute or relative to [FuelManager.instance]' base-path path
         * @param parameters [Parameters] the optional parameters
         * @return [Request] the request
         */
        fun delete(convertible: PathStringConvertible, parameters: Parameters? = null): Request

        /**
         * Create a [Method.HEAD] [Request] to [path] with [parameters]
         *
         * @param path [String] the absolute or relative to [FuelManager.instance]' base-path path
         * @param parameters [Parameters] the optional parameters
         * @return [Request] the request
         */
        fun head(path: String, parameters: Parameters? = null): Request

        /**
         * Create a [Method.HEAD] [Request] to [PathStringConvertible.path] with [parameters]
         *
         * @param convertible [PathStringConvertible] the absolute or relative to [FuelManager.instance]' base-path path
         * @param parameters [Parameters] the optional parameters
         * @return [Request] the request
         */
        fun head(convertible: PathStringConvertible, parameters: Parameters? = null): Request
    }
}