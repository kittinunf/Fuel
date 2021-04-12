package com.github.kittinunf.fuel.util

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.util.zip.DeflaterOutputStream
import java.util.zip.GZIPOutputStream

class EncodeStreamTest {
    @Test
    fun gzipEncodingTest() {
        val outputStream = ByteArrayOutputStream(32)
        val result = outputStream.encode("gzip")
        assertTrue(result is GZIPOutputStream)
    }

    @Test
    fun deflateEncodingTest() {
        val outputStream = ByteArrayOutputStream(32)
        val result = outputStream.encode("deflate")
        assertTrue(result is DeflaterOutputStream)
    }

    @Test
    fun inflateEncodingTest() {
        val outputStream = ByteArrayOutputStream(32)
        val result = outputStream.encode("inflate")
        assertTrue(result is DeflaterOutputStream)
    }

    @Test
    fun chunkedEncodingTest() {
        val outputStream = ByteArrayOutputStream(32)
        val result = outputStream.encode("chunked")
        assertTrue(result is ByteArrayOutputStream)
    }

    @Test
    fun identityEncodingTest() {
        val outputStream = ByteArrayOutputStream(32)
        val result = outputStream.encode("identity")
        assertTrue(result is ByteArrayOutputStream)
    }

    @Test
    fun emptyEncodingTest() {
        val outputStream = ByteArrayOutputStream(32)
        val result = outputStream.encode("")
        assertTrue(result is ByteArrayOutputStream)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun unsupportedEncodingTest() {
        val outputStream = ByteArrayOutputStream(32)
        outputStream.encode("sdch")
    }
}
