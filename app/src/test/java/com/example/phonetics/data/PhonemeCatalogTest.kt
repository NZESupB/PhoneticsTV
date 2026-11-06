package com.example.phonetics.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** 校验 48 音数据的结构约束，防止后续增删示例词时破坏出题前提。 */
class PhonemeCatalogTest {
    @Test
    fun `总数为 48，元音 20、辅音 28`() {
        assertEquals(48, PhonemeCatalog.all.size)
        assertEquals(20, PhonemeCatalog.byCategory(PhonemeCategory.VOWEL).size)
        assertEquals(28, PhonemeCatalog.byCategory(PhonemeCategory.CONSONANT).size)
    }

    @Test
    fun `各分组数量符合 DJ 体系`() {
        val expected =
            mapOf(
                PhonemeGroup.MONOPHTHONG to 12,
                PhonemeGroup.DIPHTHONG to 8,
                PhonemeGroup.PLOSIVE to 6,
                PhonemeGroup.FRICATIVE to 10,
                PhonemeGroup.AFFRICATE to 6,
                PhonemeGroup.NASAL to 3,
                PhonemeGroup.LATERAL to 1,
                PhonemeGroup.SEMIVOWEL to 2,
            )
        expected.forEach { (group, count) ->
            assertEquals("分组 $group 数量不符", count, PhonemeCatalog.all.count { it.group == group })
        }
    }

    @Test
    fun `id 唯一且可作为文件名`() {
        val ids = PhonemeCatalog.all.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
        ids.forEach { id ->
            assertTrue("id 必须为小写 ASCII: $id", id.matches(Regex("[a-z_]+")))
        }
    }

    @Test
    fun `显示符号唯一`() {
        val symbols = PhonemeCatalog.all.map { it.symbol }
        assertEquals(symbols.size, symbols.toSet().size)
    }

    @Test
    fun `显示符号都能在目录中按符号找到自身`() {
        // 出题结果只保存音标 id；这里确保证 id 与 symbol 双向一致，便于谜底回查。
        PhonemeCatalog.all.forEach { phoneme ->
            assertEquals(phoneme, PhonemeCatalog.byId(phoneme.id))
        }
    }

    @Test
    fun `每个音标都有发音要领和至少两个示例词`() {
        PhonemeCatalog.all.forEach { phoneme ->
            assertTrue("${phoneme.id} 缺少发音要领", phoneme.tip.isNotBlank())
            assertTrue("${phoneme.id} 示例词不足", phoneme.examples.size >= 2)
            phoneme.examples.forEach { word ->
                assertTrue("${phoneme.id} 的 ${word.text} 缺少中文释义", word.meaning.isNotBlank())
                assertTrue("${phoneme.id} 的 ${word.text} 单词拼写为空", word.text.isNotBlank())
            }
        }
    }

    @Test
    fun `示例词的音素序列只使用目录中存在的音标`() {
        PhonemeCatalog.all.forEach { phoneme ->
            phoneme.examples.forEach { word ->
                word.phonemes.forEach { symbol ->
                    assertTrue(
                        "${phoneme.id} 的 ${word.text} 含未知音素 $symbol",
                        PhonemeCatalog.all.any { it.symbol == symbol },
                    )
                }
            }
        }
    }

    @Test
    fun `每个示例词都确实包含所属音标`() {
        PhonemeCatalog.all.forEach { phoneme ->
            phoneme.examples.forEach { word ->
                assertTrue(
                    "${phoneme.id} 的示例词 ${word.text} 不包含该音",
                    word.phonemes.contains(phoneme.symbol),
                )
            }
        }
    }

    @Test
    fun `分组展示覆盖该大类全部音标`() {
        PhonemeCategory.entries.forEach { category ->
            val grouped = PhonemeCatalog.groupedByCategory(category).flatMap { it.second }
            assertEquals(PhonemeCatalog.byCategory(category), grouped)
        }
    }

    @Test
    fun `未知 id 会显式失败`() {
        val error = runCatching { PhonemeCatalog.byId("not_a_phoneme") }.exceptionOrNull()
        assertTrue(error is IllegalArgumentException)
    }
}
