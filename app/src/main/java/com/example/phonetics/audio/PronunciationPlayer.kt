package com.example.phonetics.audio

import com.example.phonetics.data.ExampleWord
import com.example.phonetics.data.Phoneme
import kotlinx.coroutines.flow.StateFlow

/** 播放来源，用于在界面上如实告知用户"听到的是什么"。 */
enum class PlaybackSource {
    /** assets 中的音标录音。 */
    PHONEME_RECORDING,

    /** assets 中预生成的示例单词录音。 */
    WORD_RECORDING,

    /** 在线获取并缓存的单词发音（assets 中没有该词录音时）。 */
    ONLINE,

    /** 设备 TTS 朗读示例单词（缺少录音时的替代方案）。 */
    SPEECH,
}

/** 播放器对外暴露的状态，界面据此给出可见反馈而不是静默失败。 */
sealed interface PlayerStatus {
    data object Idle : PlayerStatus

    data class Playing(
        val label: String,
        val source: PlaybackSource,
    ) : PlayerStatus

    /** 正在联网获取 [label] 的发音，获取失败会回退到设备朗读。 */
    data class Loading(
        val label: String,
    ) : PlayerStatus

    /** 设备不支持英式 TTS 等无法播放的情形，[message] 直接展示给用户。 */
    data class Unavailable(
        val message: String,
    ) : PlayerStatus
}

/**
 * 音标发音播放。
 *
 * 约定：单个音素本身用 TTS 合成往往失真（如合成 /tʃ/ 常读成字母 "t"），
 * 因此缺少录音时一律朗读该音标的示例单词，并在界面标注朗读的是哪个词。
 */
interface PronunciationPlayer {
    val status: StateFlow<PlayerStatus>

    /** 播放音标：[word] 为界面当前展示的示例词，仅用于 TTS 兜底时朗读。 */
    fun playPhoneme(
        phoneme: Phoneme,
        word: ExampleWord,
    )

    /** 播放示例单词。 */
    fun playWord(word: ExampleWord)

    fun stop()

    fun shutdown()
}
