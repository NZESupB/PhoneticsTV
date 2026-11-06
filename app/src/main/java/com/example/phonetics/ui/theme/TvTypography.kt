package com.example.phonetics.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.phonetics.R

/**
 * 电视端字号档位。
 *
 * Android TV 的 4K 设备不会简单地"像素翻倍"，逻辑宽度可能是 960dp、1280dp 或 1920dp，
 * 同一条 30sp 标题在 960dp 屏上会占掉三分之一宽度。因此字号按**逻辑宽度**分三档，
 * 由 [localFontTier] 在运行时选出，各页面通过 [TvTypography] 取值。
 */
enum class FontTier {
    /** 逻辑宽度 < 1000dp：高 density 的 4K 盒子、720p 面板。 */
    COMPACT,

    /** 1000dp ~ 1500dp：常见 1080p 电视。 */
    REGULAR,

    /** >= 1500dp：逻辑空间更大的 4K 设备。 */
    LARGE,
}

/** 字号集合，与 res/values/dimens.xml 的命名一一对应。 */
@Immutable
data class TvTypography(
    val browseTitle: TextUnit,
    val browseTab: TextUnit,
    val browsePreviewTitle: TextUnit,
    val browsePreviewTip: TextUnit,
    val browsePreviewExamples: TextUnit,
    val browseGroupLabel: TextUnit,
    val browseCardSymbol: TextUnit,
    val browseCardWord: TextUnit,
    val detailHeader: TextUnit,
    val detailSymbol: TextUnit,
    val detailTip: TextUnit,
    val detailSection: TextUnit,
    val detailWord: TextUnit,
    val detailTranscription: TextUnit,
    val detailButton: TextUnit,
    val detailStatus: TextUnit,
    val quizTitle: TextUnit,
    val quizMeta: TextUnit,
    val quizPrompt: TextUnit,
    val quizPromptSymbol: TextUnit,
    val quizOption: TextUnit,
    val quizOptionHint: TextUnit,
    val quizFeedback: TextUnit,
    val quizButton: TextUnit,
    val quizChoice: TextUnit,
    val quizResultRow: TextUnit,
    val quizResultDetail: TextUnit,
    val quizResultHint: TextUnit,
    val quizStatus: TextUnit,
)

/** 基准档资源表：dimen 名 -> 资源 id。 */
private val BASE_DIMENS =
    mapOf(
        "browse_card_symbol_size" to R.dimen.browse_card_symbol_size,
        "browse_card_word_size" to R.dimen.browse_card_word_size,
        "browse_group_label_size" to R.dimen.browse_group_label_size,
        "browse_preview_examples_size" to R.dimen.browse_preview_examples_size,
        "browse_preview_tip_size" to R.dimen.browse_preview_tip_size,
        "browse_preview_title_size" to R.dimen.browse_preview_title_size,
        "browse_tab_size" to R.dimen.browse_tab_size,
        "browse_title_size" to R.dimen.browse_title_size,
        "detail_button_size" to R.dimen.detail_button_size,
        "detail_header_size" to R.dimen.detail_header_size,
        "detail_section_size" to R.dimen.detail_section_size,
        "detail_status_size" to R.dimen.detail_status_size,
        "detail_symbol_size" to R.dimen.detail_symbol_size,
        "detail_tip_size" to R.dimen.detail_tip_size,
        "detail_transcription_size" to R.dimen.detail_transcription_size,
        "detail_word_size" to R.dimen.detail_word_size,
        "quiz_button_size" to R.dimen.quiz_button_size,
        "quiz_choice_size" to R.dimen.quiz_choice_size,
        "quiz_feedback_size" to R.dimen.quiz_feedback_size,
        "quiz_meta_size" to R.dimen.quiz_meta_size,
        "quiz_option_hint_size" to R.dimen.quiz_option_hint_size,
        "quiz_option_size" to R.dimen.quiz_option_size,
        "quiz_prompt_size" to R.dimen.quiz_prompt_size,
        "quiz_prompt_symbol_size" to R.dimen.quiz_prompt_symbol_size,
        "quiz_result_detail_size" to R.dimen.quiz_result_detail_size,
        "quiz_result_hint_size" to R.dimen.quiz_result_hint_size,
        "quiz_result_row_size" to R.dimen.quiz_result_row_size,
        "quiz_status_size" to R.dimen.quiz_status_size,
        "quiz_title_size" to R.dimen.quiz_title_size,
    )

/** 大屏档资源表（逻辑宽 >= 1500dp）。 */
private val LARGE_DIMENS =
    mapOf(
        "browse_card_symbol_size" to R.dimen.browse_card_symbol_size_large,
        "browse_card_word_size" to R.dimen.browse_card_word_size_large,
        "browse_group_label_size" to R.dimen.browse_group_label_size_large,
        "browse_preview_examples_size" to R.dimen.browse_preview_examples_size_large,
        "browse_preview_tip_size" to R.dimen.browse_preview_tip_size_large,
        "browse_preview_title_size" to R.dimen.browse_preview_title_size_large,
        "browse_tab_size" to R.dimen.browse_tab_size_large,
        "browse_title_size" to R.dimen.browse_title_size_large,
        "detail_button_size" to R.dimen.detail_button_size_large,
        "detail_header_size" to R.dimen.detail_header_size_large,
        "detail_section_size" to R.dimen.detail_section_size_large,
        "detail_status_size" to R.dimen.detail_status_size_large,
        "detail_symbol_size" to R.dimen.detail_symbol_size_large,
        "detail_tip_size" to R.dimen.detail_tip_size_large,
        "detail_transcription_size" to R.dimen.detail_transcription_size_large,
        "detail_word_size" to R.dimen.detail_word_size_large,
        "quiz_button_size" to R.dimen.quiz_button_size_large,
        "quiz_choice_size" to R.dimen.quiz_choice_size_large,
        "quiz_feedback_size" to R.dimen.quiz_feedback_size_large,
        "quiz_meta_size" to R.dimen.quiz_meta_size_large,
        "quiz_option_hint_size" to R.dimen.quiz_option_hint_size_large,
        "quiz_option_size" to R.dimen.quiz_option_size_large,
        "quiz_prompt_size" to R.dimen.quiz_prompt_size_large,
        "quiz_prompt_symbol_size" to R.dimen.quiz_prompt_symbol_size_large,
        "quiz_result_detail_size" to R.dimen.quiz_result_detail_size_large,
        "quiz_result_hint_size" to R.dimen.quiz_result_hint_size_large,
        "quiz_result_row_size" to R.dimen.quiz_result_row_size_large,
        "quiz_status_size" to R.dimen.quiz_status_size_large,
        "quiz_title_size" to R.dimen.quiz_title_size_large,
    )

/** 紧凑档资源表（逻辑宽 < 1000dp）。 */
private val COMPACT_DIMENS =
    mapOf(
        "browse_card_symbol_size" to R.dimen.browse_card_symbol_size_compact,
        "browse_card_word_size" to R.dimen.browse_card_word_size_compact,
        "browse_group_label_size" to R.dimen.browse_group_label_size_compact,
        "browse_preview_examples_size" to R.dimen.browse_preview_examples_size_compact,
        "browse_preview_tip_size" to R.dimen.browse_preview_tip_size_compact,
        "browse_preview_title_size" to R.dimen.browse_preview_title_size_compact,
        "browse_tab_size" to R.dimen.browse_tab_size_compact,
        "browse_title_size" to R.dimen.browse_title_size_compact,
        "detail_button_size" to R.dimen.detail_button_size_compact,
        "detail_header_size" to R.dimen.detail_header_size_compact,
        "detail_section_size" to R.dimen.detail_section_size_compact,
        "detail_status_size" to R.dimen.detail_status_size_compact,
        "detail_symbol_size" to R.dimen.detail_symbol_size_compact,
        "detail_tip_size" to R.dimen.detail_tip_size_compact,
        "detail_transcription_size" to R.dimen.detail_transcription_size_compact,
        "detail_word_size" to R.dimen.detail_word_size_compact,
        "quiz_button_size" to R.dimen.quiz_button_size_compact,
        "quiz_choice_size" to R.dimen.quiz_choice_size_compact,
        "quiz_feedback_size" to R.dimen.quiz_feedback_size_compact,
        "quiz_meta_size" to R.dimen.quiz_meta_size_compact,
        "quiz_option_hint_size" to R.dimen.quiz_option_hint_size_compact,
        "quiz_option_size" to R.dimen.quiz_option_size_compact,
        "quiz_prompt_size" to R.dimen.quiz_prompt_size_compact,
        "quiz_prompt_symbol_size" to R.dimen.quiz_prompt_symbol_size_compact,
        "quiz_result_detail_size" to R.dimen.quiz_result_detail_size_compact,
        "quiz_result_hint_size" to R.dimen.quiz_result_hint_size_compact,
        "quiz_result_row_size" to R.dimen.quiz_result_row_size_compact,
        "quiz_status_size" to R.dimen.quiz_status_size_compact,
        "quiz_title_size" to R.dimen.quiz_title_size_compact,
    )

/**
 * 按档位取资源 id，缺失时回落到基准档。
 *
 * 用显式资源表而不是反射：R 字段是编译期常量，表写错会在编译阶段直接报错。
 */
private fun resourceForTier(
    name: String,
    tier: FontTier,
): Int {
    val table =
        when (tier) {
            FontTier.COMPACT -> COMPACT_DIMENS
            FontTier.REGULAR -> BASE_DIMENS
            FontTier.LARGE -> LARGE_DIMENS
        }
    return table[name] ?: requireNotNull(BASE_DIMENS[name]) { "未知字号资源：$name" }
}

/** 读取 dimen 资源并按 density 换算成 sp（不重复缩放）。 */
@Composable
@ReadOnlyComposable
private fun font(
    name: String,
    tier: FontTier,
): TextUnit {
    val resources = LocalContext.current.resources
    val density = LocalDensity.current.density
    return (resources.getDimension(resourceForTier(name, tier)) / density).sp
}

@Composable
@ReadOnlyComposable
fun rememberTvTypography(tier: FontTier): TvTypography =
    TvTypography(
        browseTitle = font("browse_title_size", tier),
        browseTab = font("browse_tab_size", tier),
        browsePreviewTitle = font("browse_preview_title_size", tier),
        browsePreviewTip = font("browse_preview_tip_size", tier),
        browsePreviewExamples = font("browse_preview_examples_size", tier),
        browseGroupLabel = font("browse_group_label_size", tier),
        browseCardSymbol = font("browse_card_symbol_size", tier),
        browseCardWord = font("browse_card_word_size", tier),
        detailHeader = font("detail_header_size", tier),
        detailSymbol = font("detail_symbol_size", tier),
        detailTip = font("detail_tip_size", tier),
        detailSection = font("detail_section_size", tier),
        detailWord = font("detail_word_size", tier),
        detailTranscription = font("detail_transcription_size", tier),
        detailButton = font("detail_button_size", tier),
        detailStatus = font("detail_status_size", tier),
        quizTitle = font("quiz_title_size", tier),
        quizMeta = font("quiz_meta_size", tier),
        quizPrompt = font("quiz_prompt_size", tier),
        quizPromptSymbol = font("quiz_prompt_symbol_size", tier),
        quizOption = font("quiz_option_size", tier),
        quizOptionHint = font("quiz_option_hint_size", tier),
        quizFeedback = font("quiz_feedback_size", tier),
        quizButton = font("quiz_button_size", tier),
        quizChoice = font("quiz_choice_size", tier),
        quizResultRow = font("quiz_result_row_size", tier),
        quizResultDetail = font("quiz_result_detail_size", tier),
        quizResultHint = font("quiz_result_hint_size", tier),
        quizStatus = font("quiz_status_size", tier),
    )

val LocalTvTypography =
    staticCompositionLocalOf<TvTypography> {
        error("LocalTvTypography 未提供，请在 PhoneticsTvTheme 内使用")
    }

/** 当前设备的字号档位，供布局做结构上的区分（例如是否显示预览头）。 */
val LocalFontTier = staticCompositionLocalOf { FontTier.REGULAR }

/** 按逻辑宽度判定字号档位。 */
fun fontTierFor(screenWidthDp: Int): FontTier =
    when {
        screenWidthDp < 1000 -> FontTier.COMPACT
        screenWidthDp < 1500 -> FontTier.REGULAR
        else -> FontTier.LARGE
    }

@Composable
fun ProvideTvTypography(content: @Composable () -> Unit) {
    val configuration = LocalConfiguration.current
    val widthDp = configuration.screenWidthDp
    val tier = fontTierFor(widthDp)
    // 电视端 4K 适配的关键事实：逻辑宽度比像素数更能说明布局可用空间，排查显示问题时先看这一行。
    android.util.Log.i(
        "TvTypography",
        "screenWidthDp=$widthDp densityDpi=${configuration.densityDpi} tier=$tier",
    )
    val typography = rememberTvTypography(tier)
    CompositionLocalProvider(
        LocalTvTypography provides typography,
        LocalFontTier provides tier,
        content = content,
    )
}
