package com.example.phonetics.audio

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.phonetics.data.ExampleWord
import com.example.phonetics.data.Phoneme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.Locale
import java.util.UUID

/**
 * 优先播放 assets 中的录音，其次在线发音，最后用设备 TTS（英式英语）朗读示例单词。
 *
 * 音标查找顺序：
 * 1. [PHONEME_DIR]/<phoneme.id>.<ext>
 * 2. TTS 朗读示例词 [ExampleWord.text]
 *
 * 单词查找顺序：
 * 1. [WORD_DIR]/<word.text>.<ext>
 * 2. [OnlineWordAudio] 的本地缓存
 * 3. [OnlineWordAudio] 在线下载（期间状态为 [PlayerStatus.Loading]）
 * 4. TTS 朗读 [ExampleWord.text]
 *
 * 所有公开方法都应在主线程调用；在线下载在 IO 线程执行，结果回到主线程播放。
 */
class AssetOrTtsPlayer(
    private val context: Context,
    private val online: OnlineWordAudio = OnlineWordAudio(File(context.cacheDir, ONLINE_CACHE_DIR)),
) : PronunciationPlayer {
    private val _status = MutableStateFlow<PlayerStatus>(PlayerStatus.Idle)
    override val status: StateFlow<PlayerStatus> = _status.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    /** 每次 [stop] 自增；在线下载完成时序号已变，说明用户已切换或停止，结果只缓存不播放。 */
    private var requestSeq = 0

    /** TTS 初始化是异步的，初始化完成前发出的请求暂存于此。 */
    private var pending: (() -> Unit)? = null
    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var ttsFailed = false

    init {
        tts =
            TextToSpeech(context.applicationContext) { result ->
                ttsReady = result == TextToSpeech.SUCCESS
                if (!ttsReady) {
                    ttsFailed = true
                    _status.value = PlayerStatus.Unavailable(TTS_INIT_FAILED)
                    pending = null
                    return@TextToSpeech
                }
                val localeResult = tts?.setLanguage(Locale.UK)
                if (localeResult == TextToSpeech.LANG_MISSING_DATA || localeResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    ttsFailed = true
                    _status.value = PlayerStatus.Unavailable(TTS_LOCALE_MISSING)
                } else {
                    tts?.setOnUtteranceProgressListener(
                        object : UtteranceProgressListener() {
                            override fun onStart(utteranceId: String?) = Unit

                            override fun onDone(utteranceId: String?) {
                                _status.value = PlayerStatus.Idle
                            }

                            @Deprecated("兼容 API 21")
                            override fun onError(utteranceId: String?) {
                                _status.value = PlayerStatus.Unavailable(TTS_SPEAK_FAILED)
                            }

                            override fun onError(
                                utteranceId: String?,
                                errorCode: Int,
                            ) {
                                Log.w(TAG, "TTS 朗读失败，错误码 $errorCode")
                                _status.value = PlayerStatus.Unavailable(TTS_SPEAK_FAILED)
                            }
                        },
                    )
                }
                pending?.invoke()
                pending = null
            }
    }

    override fun playPhoneme(
        phoneme: Phoneme,
        word: ExampleWord,
    ) {
        stop()
        val recording = findRecording(PHONEME_DIR, phoneme.id)
        if (recording != null && playAsset(recording, phoneme.symbol, PlaybackSource.PHONEME_RECORDING)) {
            return
        }
        // 没有音素录音，或录音损坏播放失败：退回到朗读示例词，并如实标注来源。
        speak(word, phoneme)
    }

    override fun playWord(word: ExampleWord) {
        stop()
        val recording = findRecording(WORD_DIR, word.text)
        if (recording != null && playAsset(recording, word.text, PlaybackSource.WORD_RECORDING)) {
            return
        }
        val cached = online.cached(word.text)
        if (cached != null) {
            if (playFile(cached, word.text, PlaybackSource.ONLINE)) return
            // 缓存文件损坏：删掉后重新下载，而不是每次都卡在同一个坏文件上。
            online.evict(word.text)
        }
        fetchAndPlay(word)
    }

    override fun stop() {
        requestSeq++
        mediaPlayer?.let { player ->
            // 先 stop 再 release：已释放的 MediaPlayer 调用 isPlaying 会抛 IllegalStateException。
            runCatching { player.stop() }
            runCatching { player.release() }
        }
        mediaPlayer = null
        pending = null
        if (ttsReady) tts?.stop()
        val current = _status.value
        if (current is PlayerStatus.Playing || current is PlayerStatus.Loading) _status.value = PlayerStatus.Idle
    }

    override fun shutdown() {
        stop()
        scope.cancel()
        tts?.shutdown()
        tts = null
        ttsReady = false
    }

    /** 在线下载单词发音后播放；下载或播放失败时回退到设备 TTS，并在界面说明原因。 */
    private fun fetchAndPlay(word: ExampleWord) {
        val request = requestSeq
        _status.value = PlayerStatus.Loading(word.text)
        scope.launch {
            val file =
                try {
                    withContext(Dispatchers.IO) { online.download(word.text) }
                } catch (e: IOException) {
                    Log.w(TAG, "在线发音获取失败：${word.text}", e)
                    null
                }
            if (request != requestSeq) return@launch
            if (file != null) {
                if (playFile(file, word.text, PlaybackSource.ONLINE)) return@launch
                online.evict(word.text)
            }
            if (ttsFailed) {
                _status.value = PlayerStatus.Unavailable(ONLINE_AND_TTS_FAILED)
            } else {
                speak(word, phoneme = null)
            }
        }
    }

    /** 播放 assets 录音；返回 false 表示没播成，调用方可回退到朗读。 */
    private fun playAsset(
        assetPath: String,
        label: String,
        source: PlaybackSource,
    ): Boolean {
        val descriptor: AssetFileDescriptor =
            try {
                context.assets.openFd(assetPath)
            } catch (e: IOException) {
                // 录音存在性检查与打开之间出现异常（如资源被清理），退回到 TTS 而不是静默无声。
                Log.w(TAG, "打开录音失败：$assetPath", e)
                return false
            }
        try {
            return startPlayback(label, source) {
                it.setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
            }
        } finally {
            descriptor.close()
        }
    }

    /** 播放本地文件（在线发音缓存）；返回 false 表示没播成。 */
    private fun playFile(
        file: File,
        label: String,
        source: PlaybackSource,
    ): Boolean = startPlayback(label, source) { it.setDataSource(file.path) }

    /** 用 MediaPlayer 准备并播放 [setDataSource] 指定的音频；返回 false 表示没播成，调用方可回退。 */
    private fun startPlayback(
        label: String,
        source: PlaybackSource,
        setDataSource: (MediaPlayer) -> Unit,
    ): Boolean {
        val player = MediaPlayer()
        mediaPlayer = player
        try {
            player.setAudioAttributes(
                AudioAttributes
                    .Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build(),
            )
            setDataSource(player)
            player.setOnCompletionListener {
                _status.value = PlayerStatus.Idle
                it.release()
                if (mediaPlayer === it) mediaPlayer = null
            }
            player.setOnErrorListener { mp, what, extra ->
                Log.w(TAG, "录音播放失败 what=$what extra=$extra")
                _status.value = PlayerStatus.Unavailable("录音播放失败：$label")
                mp.release()
                if (mediaPlayer === mp) mediaPlayer = null
                true
            }
            player.prepare()
            player.start()
            _status.value = PlayerStatus.Playing(label, source)
            return true
        } catch (e: IOException) {
            Log.w(TAG, "录音播放异常：$label", e)
            runCatching { player.release() }
            if (mediaPlayer === player) mediaPlayer = null
            return false
        }
    }

    private fun speak(
        word: ExampleWord,
        phoneme: Phoneme?,
    ) {
        val label = phoneme?.let { "单词 ${word.text}" } ?: word.text
        val utterance = {
            if (ttsFailed) {
                if (_status.value !is PlayerStatus.Unavailable) {
                    _status.value = PlayerStatus.Unavailable(TTS_LOCALE_MISSING)
                }
            } else {
                val utteranceId = UUID.randomUUID().toString()
                val result = tts?.speak(word.text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
                if (result == TextToSpeech.SUCCESS) {
                    _status.value = PlayerStatus.Playing(label, PlaybackSource.SPEECH)
                } else {
                    _status.value = PlayerStatus.Unavailable(TTS_SPEAK_FAILED)
                }
            }
        }
        if (ttsReady || ttsFailed) utterance() else pending = utterance
    }

    /** 返回 assets 内相对路径（含目录），找不到返回 null。 */
    private fun findRecording(
        dir: String,
        name: String,
    ): String? {
        val candidates = AUDIO_EXTENSIONS.map { "$dir/$name.$it" }
        return candidates.firstOrNull { exists(it) }
    }

    private fun exists(assetPath: String): Boolean =
        runCatching {
            context.assets.open(assetPath).use { true }
        }.getOrDefault(false)

    private companion object {
        const val TAG = "PronunciationPlayer"
        const val PHONEME_DIR = "audio/phonemes"
        const val WORD_DIR = "audio/words"
        const val ONLINE_CACHE_DIR = "audio/words"
        val AUDIO_EXTENSIONS = listOf("mp3", "ogg", "m4a", "wav")

        const val TTS_INIT_FAILED = "设备文本转语音引擎初始化失败，请在系统设置中检查「文字转语音」"
        const val TTS_LOCALE_MISSING = "设备缺少英式英语语音包，请在系统设置 → 无障碍 → 文字转语音 中安装英语（英国）语音数据"
        const val TTS_SPEAK_FAILED = "语音朗读失败，请稍后重试"
        const val ONLINE_AND_TTS_FAILED = "无法获取在线发音（请检查网络连接），且设备文字转语音不可用"
    }
}
