package com.example.phonetics.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * 校验内置音标录音完整性。
 *
 * 录音缺失不会让应用报错（会回退到 TTS 朗读示例词），所以必须在构建期用测试守住，
 * 否则"某个音没声音"要等到用户在电视上点开才会发现。
 */
class PhonemeAudioAssetsTest {
    private val phonemeDir = File("src/main/assets/audio/phonemes")

    @Test
    fun `每个音标都有对应的 mp3 录音`() {
        assertTrue("找不到录音目录：${phonemeDir.absolutePath}", phonemeDir.isDirectory)

        val missing =
            PhonemeCatalog.all
                .map { it.id }
                .filterNot { File(phonemeDir, "$it.mp3").exists() }
        assertEquals("缺少录音的音标：$missing", emptyList<String>(), missing)
    }

    @Test
    fun `录音目录里没有多余的孤儿文件`() {
        val knownIds = PhonemeCatalog.all.map { it.id }.toSet()
        val orphans =
            phonemeDir
                .listFiles()
                ?.map { it.name.removeSuffix(".mp3") }
                ?.filterNot { it in knownIds }
                .orEmpty()
        assertEquals("未对应任何音标的文件：$orphans", emptyList<String>(), orphans)
    }

    @Test
    fun `每个录音都是可解码的 mp3 且体量合理`() {
        PhonemeCatalog.all.forEach { phoneme ->
            val file = File(phonemeDir, "${phoneme.id}.mp3")
            val bytes = file.readBytes()
            assertTrue("${phoneme.id} 文件过小，可能是空文件或错误页", bytes.size > 8 * 1024)

            // MP3 以 ID3 标签或 MPEG 帧同步字(0xFF 0xEx/0xFx)开头，可据此排除下载到的 HTML 错误页。
            val header = bytes.take(3).toByteArray()
            val hasId3 =
                header.size == 3 &&
                    header[0] == 'I'.code.toByte() &&
                    header[1] == 'D'.code.toByte() &&
                    header[2] == '3'.code.toByte()
            val first = bytes[0].toInt() and 0xFF
            val second = bytes[1].toInt() and 0xFF
            val hasFrameSync = first == 0xFF && (second and 0xE0) == 0xE0
            assertTrue("${phoneme.id} 不是有效的 MP3 文件", hasId3 || hasFrameSync)
        }
    }

    @Test
    fun `录音总大小控制在合理范围`() {
        val total = phonemeDir.listFiles()?.sumOf { it.length() } ?: 0
        // 当前约 2.1 MB；超过 8 MB 会明显拖大 APK，应在合入前压缩。
        assertTrue("录音总体积过大：${total / 1024} KB", total < 8L * 1024 * 1024)
    }
}
