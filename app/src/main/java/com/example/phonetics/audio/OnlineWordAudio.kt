package com.example.phonetics.audio

import androidx.annotation.WorkerThread
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

/**
 * 在线单词发音：assets 里没有该词录音时，按 [urlFor] 下载音频并缓存到 [cacheDir]，同一个词只请求一次。
 *
 * 纯 JVM 实现（只用 java.net），不依赖 Android 运行时，便于单元测试。
 * 默认音源是有道词典英音（type=1）；换成自建代理或其他服务时只需替换 [urlFor]，
 * 不要把需要密钥的云 TTS 直接写进这里（密钥会随 APK 泄露）。
 */
class OnlineWordAudio(
    private val cacheDir: File,
    private val urlFor: (String) -> String = ::youdaoUkUrl,
) {
    /** 已缓存的音频文件；没有缓存返回 null。 */
    fun cached(word: String): File? = cacheFile(word).takeIf { it.length() > 0 }

    /** 删除损坏的缓存，下次播放会重新下载。 */
    fun evict(word: String) {
        cacheFile(word).delete()
    }

    /**
     * 下载并缓存 [word] 的发音，返回缓存文件。
     *
     * 阻塞调用，必须在 IO 线程执行。HTTP 非 200、响应不是音频（如 HTML 错误页）、内容为空或超时，
     * 都抛 [IOException]，由调用方决定回退方式；失败时不会留下半截缓存文件。
     */
    @WorkerThread
    @Throws(IOException::class)
    fun download(word: String): File {
        val target = cacheFile(word)
        val connection = URL(urlFor(word)).openConnection() as HttpURLConnection
        connection.connectTimeout = CONNECT_TIMEOUT_MS
        connection.readTimeout = READ_TIMEOUT_MS
        try {
            val code = connection.responseCode
            if (code != HttpURLConnection.HTTP_OK) throw IOException("在线发音请求失败：HTTP $code")
            val type = connection.contentType.orEmpty()
            if (!type.startsWith("audio/")) throw IOException("在线发音返回的不是音频：$type")

            if (!cacheDir.isDirectory && !cacheDir.mkdirs()) throw IOException("无法创建缓存目录：$cacheDir")
            // 先写临时文件再改名，避免中途失败或并发下载留下不完整的缓存。
            val temp = File.createTempFile("download", ".part", cacheDir)
            try {
                connection.inputStream.use { input -> temp.outputStream().use { input.copyTo(it) } }
                if (temp.length() == 0L) throw IOException("在线发音内容为空")
                if (!temp.renameTo(target)) throw IOException("写入发音缓存失败：$target")
            } finally {
                temp.delete()
            }
            return target
        } finally {
            connection.disconnect()
        }
    }

    /** 文件名用编码后的单词，含空格、撇号等字符的词也能安全落盘。 */
    private fun cacheFile(word: String): File =
        File(
            cacheDir,
            URLEncoder.encode(word.lowercase(Locale.ROOT), "UTF-8") + ".audio",
        )

    companion object {
        private const val YOUDAO_UK_URL = "https://dict.youdao.com/dictvoice?type=1&audio="
        private const val CONNECT_TIMEOUT_MS = 4_000
        private const val READ_TIMEOUT_MS = 6_000

        /** 有道词典英音地址；单词做 URL 编码，含空格或撇号的词也能正确请求。 */
        fun youdaoUkUrl(word: String): String = YOUDAO_UK_URL + URLEncoder.encode(word, "UTF-8")
    }
}
