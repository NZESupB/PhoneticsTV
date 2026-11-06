package com.example.phonetics.quiz

import com.example.phonetics.data.ExampleWord
import com.example.phonetics.data.Phoneme
import com.example.phonetics.data.PhonemeCatalog
import com.example.phonetics.data.PhonemeCategory
import kotlin.random.Random

/** 题型。 */
enum class QuizType(
    val label: String,
    val instruction: String,
) {
    /** 播放发音（录音或朗读示例词），从 4 个音标中选出正确的一个。 */
    LISTEN_PICK_SYMBOL("听音辨标", "听发音，选出对应的音标"),

    /** 显示音标，从 4 个单词中选出含有该音的单词。 */
    SEE_SYMBOL_PICK_WORD("看标选词", "选出含有这个音的单词"),
}

/** 出题范围。 */
enum class QuizScope(
    val label: String,
) {
    VOWELS("元音"),
    CONSONANTS("辅音"),
    ALL("全部"),
    ;

    /** 该范围允许出现的音标。 */
    fun phonemes(catalog: List<Phoneme> = PhonemeCatalog.all): List<Phoneme> =
        when (this) {
            VOWELS -> catalog.filter { it.category == PhonemeCategory.VOWEL }
            CONSONANTS -> catalog.filter { it.category == PhonemeCategory.CONSONANT }
            ALL -> catalog
        }
}

/** 一轮练习的配置。 */
data class QuizConfig(
    val type: QuizType,
    val scope: QuizScope,
    val questionCount: Int = DEFAULT_QUESTION_COUNT,
) {
    init {
        require(questionCount > 0) { "题目数量必须为正数：$questionCount" }
    }

    companion object {
        const val DEFAULT_QUESTION_COUNT = 10
    }
}

/** 一个选项：[text] 是音标符号或单词拼写，[hint] 是辅助信息（发音要领或音标+释义）。 */
data class QuizOption(
    val key: String,
    val text: String,
    val hint: String,
    val isCorrect: Boolean,
)

/**
 * 一道题。
 *
 * "听音辨标"时界面播放 [promptWord]（有录音播录音，否则朗读单词），不展示 [phoneme]；
 * "看标选词"时界面展示 [phoneme]。
 */
data class QuizQuestion(
    val phoneme: Phoneme,
    val promptWord: ExampleWord,
    val options: List<QuizOption>,
) {
    val correctOption: QuizOption get() = options.first { it.isCorrect }
}

/**
 * 出题器。纯 Kotlin，[random] 可注入种子以便测试复现。
 *
 * 唯一正确答案的保证：
 * - 听音辨标：干扰音标不得出现在题目单词的音素序列里（否则听到的就是对的）；
 * - 看标选词：干扰词不得含有目标音。
 */
class QuizGenerator(
    private val catalog: List<Phoneme> = PhonemeCatalog.all,
    private val random: Random = Random.Default,
) {
    init {
        require(catalog.size >= MIN_POOL_SIZE) { "音标池过小，无法出选择题：${catalog.size}" }
    }

    fun generate(config: QuizConfig): List<QuizQuestion> {
        val scoped = config.scope.phonemes(catalog)
        check(scoped.size >= MIN_POOL_SIZE) { "范围「${config.scope.label}」内音标过少，无法出题" }
        return scoped
            .shuffled(random)
            .take(config.questionCount)
            .map { phoneme ->
                when (config.type) {
                    QuizType.LISTEN_PICK_SYMBOL -> listenQuestion(phoneme, scoped)
                    QuizType.SEE_SYMBOL_PICK_WORD -> wordQuestion(phoneme)
                }
            }
    }

    private fun listenQuestion(
        phoneme: Phoneme,
        scoped: List<Phoneme>,
    ): QuizQuestion {
        val word = phoneme.examples.shuffled(random).first()
        val wordSymbols = word.phonemes.toSet()
        val candidates = scoped.filter { it.id != phoneme.id && it.symbol !in wordSymbols }
        check(candidates.size >= OPTION_COUNT - 1) { "可用干扰音标不足，无法为 ${phoneme.id} 出题" }
        // 同一发音分组的音更易混淆，优先作为干扰项。
        val distractors =
            (
                candidates.filter { it.group == phoneme.group }.shuffled(random) +
                    candidates.filter { it.group != phoneme.group }.shuffled(random)
            ).take(OPTION_COUNT - 1)
        val options =
            (distractors + phoneme)
                .map { candidate ->
                    QuizOption(
                        key = candidate.id,
                        text = candidate.symbol,
                        hint = candidate.tip,
                        isCorrect = candidate.id == phoneme.id,
                    )
                }.shuffled(random)
        return QuizQuestion(phoneme, word, options)
    }

    private fun wordQuestion(phoneme: Phoneme): QuizQuestion {
        val correct = phoneme.examples.shuffled(random).first()
        val wrong =
            catalog
                .flatMap { it.examples }
                .filter { word -> word.text != correct.text && phoneme.symbol !in word.phonemes }
                .distinctBy { it.text }
                .shuffled(random)
                .take(OPTION_COUNT - 1)
        check(wrong.size == OPTION_COUNT - 1) { "不含 ${phoneme.symbol} 的单词不足，无法为 ${phoneme.id} 出题" }
        val options =
            (wrong.map { it to false } + (correct to true))
                .map { (word, isCorrect) ->
                    QuizOption(
                        key = word.text,
                        text = word.text,
                        hint = "${word.transcription} ${word.meaning}",
                        isCorrect = isCorrect,
                    )
                }.shuffled(random)
        return QuizQuestion(phoneme, correct, options)
    }

    companion object {
        const val OPTION_COUNT = 4

        /** 至少要有 4 个音标才能凑出一个正确项加三个干扰项。 */
        private const val MIN_POOL_SIZE = OPTION_COUNT
    }
}
