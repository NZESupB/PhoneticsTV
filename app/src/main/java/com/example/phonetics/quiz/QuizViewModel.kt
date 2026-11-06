package com.example.phonetics.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.phonetics.data.Phoneme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** 一道题的回答记录，用于结果页统计与错题回顾。 */
data class AnsweredQuestion(
    val question: QuizQuestion,
    val selectedKey: String,
) {
    val selectedOption: QuizOption get() = question.options.first { it.key == selectedKey }

    val isCorrect: Boolean get() = selectedOption.isCorrect
}

/** 练习页状态。 */
sealed interface QuizUiState {
    /** 尚未开始：展示题型与范围选择。 */
    data object Setup : QuizUiState

    data class Running(
        val config: QuizConfig,
        val question: QuizQuestion,
        val questionIndex: Int,
        val total: Int,
        /** 已选中的选项 key；为空表示本题尚未作答，作答后锁定并展示反馈。 */
        val selectedKey: String? = null,
    ) : QuizUiState {
        val answered: Boolean get() = selectedKey != null

        val isCorrect: Boolean
            get() = selectedKey?.let { key -> question.options.first { it.key == key }.isCorrect } == true
    }

    data class Finished(
        val config: QuizConfig,
        val answers: List<AnsweredQuestion>,
    ) : QuizUiState {
        val correctCount: Int get() = answers.count { it.isCorrect }
        val accuracyPercent: Int
            get() = if (answers.isEmpty()) 0 else correctCount * 100 / answers.size
        val wrongAnswers: List<AnsweredQuestion> get() = answers.filterNot { it.isCorrect }
    }

    /** 出题失败（数据异常）时展示原因，避免静默空转。 */
    data class Failed(
        val config: QuizConfig,
        val message: String,
    ) : QuizUiState
}

/** 请求界面播放某道题的音频；[id] 递增，用于区分同一道题的重播。 */
data class AudioRequest(
    val id: Long,
    val question: QuizQuestion,
) {
    val phoneme: Phoneme get() = question.phoneme
}

/**
 * 练习流程状态机：出题 → 作答（可反复听音）→ 判定并锁定 → 下一题 → 结果统计。
 *
 * 出题逻辑在 [QuizGenerator] 中，可注入种子随机数以便测试复现。
 */
class QuizViewModel(
    private val generator: QuizGenerator = QuizGenerator(),
) : ViewModel() {
    private val _state = MutableStateFlow<QuizUiState>(QuizUiState.Setup)
    val state: StateFlow<QuizUiState> = _state.asStateFlow()

    private val _audioRequest = MutableStateFlow<AudioRequest?>(null)
    val audioRequest: StateFlow<AudioRequest?> = _audioRequest.asStateFlow()

    private var questions: List<QuizQuestion> = emptyList()
    private val answers = mutableListOf<AnsweredQuestion>()
    private var audioRequestId = 0L

    fun start(config: QuizConfig) {
        val result = runCatching { generator.generate(config) }
        result.exceptionOrNull()?.let { error ->
            _state.value = QuizUiState.Failed(config, error.message ?: "出题失败")
            return
        }
        questions = result.getOrThrow()
        answers.clear()
        _state.value =
            QuizUiState.Running(
                config = config,
                question = questions.first(),
                questionIndex = 0,
                total = questions.size,
            )
        requestAudio(questions.first())
    }

    fun select(optionKey: String) {
        val current = _state.value as? QuizUiState.Running ?: return
        if (current.answered) return
        val option = current.question.options.firstOrNull { it.key == optionKey } ?: return
        answers += AnsweredQuestion(current.question, option.key)
        _state.value = current.copy(selectedKey = option.key)
    }

    fun next() {
        val current = _state.value as? QuizUiState.Running ?: return
        if (!current.answered) return
        val nextIndex = current.questionIndex + 1
        val nextQuestion = questions.getOrNull(nextIndex)
        if (nextQuestion == null) {
            _state.value = QuizUiState.Finished(current.config, answers.toList())
        } else {
            _state.value =
                current.copy(
                    question = nextQuestion,
                    questionIndex = nextIndex,
                    selectedKey = null,
                )
            requestAudio(nextQuestion)
        }
    }

    /** 用同一配置再练一轮。 */
    fun restart() {
        val config =
            when (val current = _state.value) {
                is QuizUiState.Running -> current.config
                is QuizUiState.Finished -> current.config
                is QuizUiState.Failed -> current.config
                QuizUiState.Setup -> return
            }
        start(config)
    }

    fun backToSetup() {
        questions = emptyList()
        answers.clear()
        _state.value = QuizUiState.Setup
    }

    /** 重播当前题音频（遥控器 MEDIA_PLAY_PAUSE 与"再听一遍"按钮）。 */
    fun replayAudio() {
        val current = _state.value as? QuizUiState.Running ?: return
        requestAudio(current.question)
    }

    /** 界面消费完播放事件后调用，避免旋转/重组时重复播放。 */
    fun consumeAudioRequest(id: Long) {
        if (_audioRequest.value?.id == id) _audioRequest.value = null
    }

    private fun requestAudio(question: QuizQuestion) {
        audioRequestId += 1
        _audioRequest.value = AudioRequest(audioRequestId, question)
    }

    companion object {
        fun factory(generator: QuizGenerator = QuizGenerator()): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T = QuizViewModel(generator) as T
            }
    }
}
