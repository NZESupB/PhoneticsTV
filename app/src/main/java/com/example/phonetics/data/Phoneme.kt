package com.example.phonetics.data

/** 音标大类：决定首页的顶级标签页。 */
enum class PhonemeCategory(
    val label: String,
) {
    VOWEL("元音"),
    CONSONANT("辅音"),
}

/** 音标细分发音方式分组，首页按分组分行展示。 */
enum class PhonemeGroup(
    val category: PhonemeCategory,
    val label: String,
) {
    MONOPHTHONG(PhonemeCategory.VOWEL, "单元音"),
    DIPHTHONG(PhonemeCategory.VOWEL, "双元音"),
    PLOSIVE(PhonemeCategory.CONSONANT, "爆破音"),
    FRICATIVE(PhonemeCategory.CONSONANT, "摩擦音"),
    AFFRICATE(PhonemeCategory.CONSONANT, "破擦音"),
    NASAL(PhonemeCategory.CONSONANT, "鼻音"),
    LATERAL(PhonemeCategory.CONSONANT, "舌侧音"),
    SEMIVOWEL(PhonemeCategory.CONSONANT, "半元音"),
    ;

    companion object {
        fun of(category: PhonemeCategory): List<PhonemeGroup> = entries.filter { it.category == category }
    }
}

/**
 * 示例单词。
 *
 * [phonemes] 是该词的 DJ 音素序列（不含重音符号），界面据此拼出音标显示；
 * 出题时也依赖它排除"本身就含目标音"的干扰词，保证每道题唯一正确。
 */
data class ExampleWord(
    val text: String,
    val phonemes: List<String>,
    val meaning: String,
) {
    /** 例如 /ʃiːp/。 */
    val transcription: String = "/" + phonemes.joinToString("") + "/"
}

/**
 * 一个音标。
 *
 * [id] 仅使用 ASCII，作为 assets 中录音文件名与导航参数；[symbol] 用于显示。
 */
data class Phoneme(
    val id: String,
    val symbol: String,
    val group: PhonemeGroup,
    val tip: String,
    val examples: List<ExampleWord>,
) {
    val category: PhonemeCategory get() = group.category
}
