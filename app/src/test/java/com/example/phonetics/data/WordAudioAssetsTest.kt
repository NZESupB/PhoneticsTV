package com.example.phonetics.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * 校验预生成的示例单词录音（由 tools/generate-word-audio.sh 生成）。
 *
 * 单词录音缺失时播放器会回退到设备 TTS，而很多电视盒子没有英式语音包，
 * 所以必须在构建期守住：新增示例词后忘了跑脚本，这里会直接失败。
 */
class WordAudioAssetsTest {
    private val wordDir = File("src/main/assets/audio/words")
    private val sourcesFile = File("../tools/word-audio-sources.tsv")
    private val words =
        PhonemeCatalog.all
            .flatMap { it.examples }
            .map { it.text }
            .toSortedSet()

    @Test
    fun `每个示例词都有对应的 mp3 录音`() {
        assertTrue("找不到单词录音目录：${wordDir.absolutePath}", wordDir.isDirectory)

        val missing = words.filterNot { File(wordDir, "$it.mp3").exists() }
        assertEquals("缺少录音的示例词（运行 tools/generate-word-audio.sh）：$missing", emptyList<String>(), missing)
    }

    @Test
    fun `单词录音目录里没有多余的孤儿文件`() {
        val orphans =
            wordDir
                .listFiles()
                ?.map { it.name }
                ?.filterNot { it.endsWith(".mp3") && it.removeSuffix(".mp3") in words }
                .orEmpty()
        assertEquals("未对应任何示例词的文件：$orphans", emptyList<String>(), orphans)
    }

    @Test
    fun `每个单词录音都是有效的 mp3`() {
        words.forEach { word ->
            val bytes = File(wordDir, "$word.mp3").readBytes()
            assertTrue("$word 文件过小，可能是空文件或错误页", bytes.size > 1024)
            assertTrue("$word 不是有效的 MP3 文件", hasMp3Header(bytes))
        }
    }

    @Test
    fun `每个单词录音都登记了来源与授权`() {
        assertTrue("找不到来源清单：${sourcesFile.absolutePath}", sourcesFile.isFile)

        val registered =
            sourcesFile
                .readLines()
                .drop(1)
                .filter { it.isNotBlank() }
                .map { it.substringBefore('\t') }
                .toSortedSet()
        assertEquals("来源清单与示例词不一致", words, registered)
    }

    @Test
    fun `单词录音总大小控制在合理范围`() {
        val total = wordDir.listFiles()?.sumOf { it.length() } ?: 0
        // 当前约 2.3 MB；超过 3 MB 应先压缩码率再合入。
        assertTrue("单词录音总体积过大：${total / 1024} KB", total < 3L * 1024 * 1024)
    }
}
