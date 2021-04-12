package com.github.kittinunf.fuel.util

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.DeflaterOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import java.util.zip.InflaterInputStream
import java.util.zip.ZipException

class DecodeStreamTest {
    @Test(expected = ZipException::class)
    fun gzipEncodingTest() {
        val inputStream = "test".byteInputStream()
        inputStream.decode("gzip")
    }

    @Test
    fun deflateEncodingTest() {
        val inputStream = ByteArrayInputStream(byteArrayOf())
        val result = inputStream.decode("deflate")
        assertTrue(result is InflaterInputStream)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun inflateEncodingTest() {
        val inputStream = ByteArrayInputStream(byteArrayOf())
        inputStream.decode("inflate")
    }

    @Test
    fun chunkedEncodingTest() {
        val inputStream = ByteArrayInputStream(byteArrayOf())
        val result = inputStream.decode("chunked")
        assertTrue(result is ByteArrayInputStream)
    }

    @Test
    fun identityEncodingTest() {
        val inputStream = ByteArrayInputStream(byteArrayOf())
        val result = inputStream.decode("identity")
        assertTrue(result is ByteArrayInputStream)
    }

    @Test
    fun emptyEncodingTest() {
        val inputStream = ByteArrayInputStream(byteArrayOf())
        val result = inputStream.decode("")
        assertTrue(result is ByteArrayInputStream)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun unsupportedEncodingTest() {
        val inputStream = ByteArrayInputStream(byteArrayOf())
        inputStream.decode("sdch")
    }
}
