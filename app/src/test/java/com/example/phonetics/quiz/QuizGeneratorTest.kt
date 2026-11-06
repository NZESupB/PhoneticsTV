package com.example.phonetics.quiz

import com.example.phonetics.data.PhonemeCatalog
import com.example.phonetics.data.PhonemeCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** 校验出题规则：唯一正确答案、范围正确、选项数量固定。 */
class QuizGeneratorTest {
    private val catalog = PhonemeCatalog.all

    private fun generator(seed: Int) = QuizGenerator(catalog, Random(seed))

    @Test
    fun `听音辨标只有唯一正确答案`() {
        repeat(20) { seed ->
            val questions =
                generator(seed).generate(
                    QuizConfig(QuizType.LISTEN_PICK_SYMBOL, QuizScope.ALL, 10),
                )
            questions.forEach { question ->
                assertEquals(
                    "第 $seed 组 ${question.phoneme.id} 的正确答案不唯一",
                    1,
                    question.options.count { it.isCorrect },
                )
            }
        }
    }

    @Test
    fun `听音辨标干扰项不出现在题目单词的音素里`() {
        repeat(20) { seed ->
            generator(seed)
                .generate(QuizConfig(QuizType.LISTEN_PICK_SYMBOL, QuizScope.ALL, 10))
                .forEach { question ->
                    val wordSymbols = question.promptWord.phonemes.toSet()
                    question.options
                        .filterNot { it.isCorrect }
                        .forEach { option ->
                            assertFalse(
                                "${question.phoneme.id} 的干扰项 ${option.text} 出现在 ${question.promptWord.text} 中",
                                option.text in wordSymbols,
                            )
                        }
                }
        }
    }

    @Test
    fun `看标选词干扰项不含目标音`() {
        repeat(20) { seed ->
            generator(seed)
                .generate(QuizConfig(QuizType.SEE_SYMBOL_PICK_WORD, QuizScope.ALL, 10))
                .forEach { question ->
                    assertEquals(1, question.options.count { it.isCorrect })
                    question.options.filterNot { it.isCorrect }.forEach { option ->
                        val wrongWord = catalog.flatMap { it.examples }.first { it.text == option.text }
                        assertFalse(
                            "${question.phoneme.symbol} 的干扰词 ${option.text} 含有该音",
                            question.phoneme.symbol in wrongWord.phonemes,
                        )
                    }
                }
        }
    }

    @Test
    fun `每题都有 4 个互不相同的选项`() {
        QuizType.entries.forEach { type ->
            QuizScope.entries.forEach { scope ->
                generator(7).generate(QuizConfig(type, scope, 10)).forEach { question ->
                    assertEquals(4, question.options.size)
                    assertEquals(
                        question.options.size,
                        question.options
                            .map { it.key }
                            .toSet()
                            .size,
                    )
                }
            }
        }
    }

    @Test
    fun `范围限定生效`() {
        val vowelTargets = generator(3).generate(QuizConfig(QuizType.LISTEN_PICK_SYMBOL, QuizScope.VOWELS, 10))
        assertTrue(vowelTargets.all { it.phoneme.category == PhonemeCategory.VOWEL })

        val consonantTargets =
            generator(3)
                .generate(QuizConfig(QuizType.SEE_SYMBOL_PICK_WORD, QuizScope.CONSONANTS, 10))
        assertTrue(consonantTargets.all { it.phoneme.category == PhonemeCategory.CONSONANT })
    }

    @Test
    fun `元音范围内选项也只出现元音`() {
        val symbols = catalog.filter { it.category == PhonemeCategory.VOWEL }.map { it.symbol }.toSet()
        val questions = generator(11).generate(QuizConfig(QuizType.LISTEN_PICK_SYMBOL, QuizScope.VOWELS, 10))
        questions.forEach { question ->
            question.options.forEach { option ->
                assertTrue("超出范围的选项 ${option.text}", option.text in symbols)
            }
        }
    }

    @Test
    fun `同一轮内不重复出同一个音标`() {
        val questions = generator(5).generate(QuizConfig(QuizType.LISTEN_PICK_SYMBOL, QuizScope.ALL, 10))
        assertEquals(questions.size, questions.map { it.phoneme.id }.toSet().size)
    }

    @Test
    fun `指定题目数量生效且不超过音标总数`() {
        val questions = generator(9).generate(QuizConfig(QuizType.LISTEN_PICK_SYMBOL, QuizScope.ALL, 5))
        assertEquals(5, questions.size)

        val all = generator(9).generate(QuizConfig(QuizType.LISTEN_PICK_SYMBOL, QuizScope.ALL, 100))
        assertEquals(catalog.size, all.size)
    }

    @Test
    fun `同一种子可复现同一套题`() {
        val first = generator(2024).generate(QuizConfig(QuizType.SEE_SYMBOL_PICK_WORD, QuizScope.ALL, 10))
        val second = generator(2024).generate(QuizConfig(QuizType.SEE_SYMBOL_PICK_WORD, QuizScope.ALL, 10))
        assertEquals(first, second)
    }

    @Test
    fun `题目数量非正数会被拒绝`() {
        val error = runCatching { QuizConfig(QuizType.LISTEN_PICK_SYMBOL, QuizScope.ALL, 0) }.exceptionOrNull()
        assertTrue(error is IllegalArgumentException)
    }
}
