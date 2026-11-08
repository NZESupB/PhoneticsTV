package com.example.phonetics.audio

import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.IOException
import java.net.InetAddress
import java.net.ServerSocket
import java.nio.file.Files
import kotlin.concurrent.thread

class OnlineWordAudioTest {
    private lateinit var cacheDir: File
    private var server: StubServer? = null

    @Before
    fun setUp() {
        cacheDir = Files.createTempDirectory("online-word-audio").toFile()
    }

    @After
    fun tearDown() {
        server?.close()
        cacheDir.deleteRecursively()
    }

    @Test
    fun `下载成功后写入缓存并可直接命中`() {
        val body = byteArrayOf(0x49, 0x44, 0x33, 1, 2, 3)
        val audio = audioFrom(StubServer(200, "audio/mpeg", body))

        assertNull(audio.cached("sheep"))
        val file = audio.download("sheep")

        assertArrayEquals(body, file.readBytes())
        assertEquals(file, audio.cached("sheep"))
        assertEquals("大小写不同的同一个词应命中同一缓存", file, audio.cached("Sheep"))
        assertEquals("不应残留临时文件", listOf(file.name), cacheDir.list()!!.toList())
    }

    @Test
    fun `响应不是音频时拒绝且不留缓存`() {
        val audio = audioFrom(StubServer(200, "text/html; charset=utf-8", "<html>error</html>".toByteArray()))

        assertDownloadFails(audio, "sheep")
        assertNull(audio.cached("sheep"))
        assertTrue("不应残留任何文件", cacheDir.list().isNullOrEmpty())
    }

    @Test
    fun `HTTP 非 200 时拒绝`() {
        val audio = audioFrom(StubServer(404, "audio/mpeg", byteArrayOf(1)))

        assertDownloadFails(audio, "sheep")
        assertNull(audio.cached("sheep"))
    }

    @Test
    fun `空内容时拒绝且不留缓存`() {
        val audio = audioFrom(StubServer(200, "audio/mpeg", ByteArray(0)))

        assertDownloadFails(audio, "sheep")
        assertTrue(cacheDir.list().isNullOrEmpty())
    }

    @Test
    fun `连接失败时抛出 IOException`() {
        val closedPort = ServerSocket(0).use { it.localPort }
        val audio = OnlineWordAudio(cacheDir) { "http://127.0.0.1:$closedPort/$it" }

        assertDownloadFails(audio, "sheep")
    }

    @Test
    fun `特殊字符的词缓存在缓存目录内`() {
        val audio = audioFrom(StubServer(200, "audio/mpeg", byteArrayOf(1, 2)))

        listOf("ice cream", "don't", "../escape").forEach { word ->
            val file = audio.download(word)
            assertEquals("$word 的缓存必须位于缓存目录下", cacheDir.canonicalFile, file.canonicalFile.parentFile)
            assertNotNull(audio.cached(word))
        }
    }

    @Test
    fun `evict 删除缓存`() {
        val audio = audioFrom(StubServer(200, "audio/mpeg", byteArrayOf(1, 2)))
        audio.download("sheep")

        audio.evict("sheep")

        assertNull(audio.cached("sheep"))
    }

    @Test
    fun `有道地址对单词做 URL 编码`() {
        assertEquals("https://dict.youdao.com/dictvoice?type=1&audio=sheep", OnlineWordAudio.youdaoUkUrl("sheep"))
        assertEquals(
            "https://dict.youdao.com/dictvoice?type=1&audio=ice+cream",
            OnlineWordAudio.youdaoUkUrl("ice cream"),
        )
        assertEquals("https://dict.youdao.com/dictvoice?type=1&audio=don%27t", OnlineWordAudio.youdaoUkUrl("don't"))
    }

    private fun audioFrom(stub: StubServer): OnlineWordAudio {
        server = stub
        return OnlineWordAudio(cacheDir) { "http://127.0.0.1:${stub.port}/word" }
    }

    private fun assertDownloadFails(
        audio: OnlineWordAudio,
        word: String,
    ) {
        try {
            audio.download(word)
            fail("应抛出 IOException")
        } catch (expected: IOException) {
            // 预期：由播放器回退到设备 TTS。
        }
    }

    /** 最小 HTTP 桩：对每个请求返回固定的状态码、Content-Type 与内容。 */
    private class StubServer(
        status: Int,
        contentType: String,
        body: ByteArray,
    ) : AutoCloseable {
        private val socket = ServerSocket(0, 0, InetAddress.getLoopbackAddress())
        val port: Int = socket.localPort

        init {
            thread(isDaemon = true) {
                while (!socket.isClosed) {
                    val client = runCatching { socket.accept() }.getOrNull() ?: break
                    client.use {
                        val reader = it.getInputStream().bufferedReader()
                        while (!reader.readLine().isNullOrEmpty()) Unit
                        val header =
                            "HTTP/1.1 $status Stub\r\n" +
                                "Content-Type: $contentType\r\n" +
                                "Content-Length: ${body.size}\r\n" +
                                "Connection: close\r\n\r\n"
                        it.getOutputStream().apply {
                            write(header.toByteArray())
                            write(body)
                            flush()
                        }
                    }
                }
            }
        }

        override fun close() = socket.close()
    }
}
