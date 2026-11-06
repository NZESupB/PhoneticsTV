package com.example.phonetics.ui.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Typography
import androidx.tv.material3.darkColorScheme
import com.example.phonetics.R

/** 电视端配色：深色背景 + 高对比文字，保证 3 米外可读。 */
private val TvColorScheme =
    darkColorScheme(
        primary = Color(0xFF4F8DFD),
        onPrimary = Color(0xFF03101F),
        primaryContainer = Color(0xFF1B3357),
        onPrimaryContainer = Color(0xFFD6E4FF),
        secondary = Color(0xFF6EE7C7),
        onSecondary = Color(0xFF00201A),
        secondaryContainer = Color(0xFF123B33),
        onSecondaryContainer = Color(0xFFC6FFF1),
        tertiary = Color(0xFFFFC46B),
        onTertiary = Color(0xFF2A1800),
        background = Color(0xFF0B1220),
        onBackground = Color(0xFFEAF1FF),
        surface = Color(0xFF14203A),
        onSurface = Color(0xFFEAF1FF),
        surfaceVariant = Color(0xFF1E2C49),
        onSurfaceVariant = Color(0xFFB7C6E0),
        border = Color(0xFF3A4B6B),
        borderVariant = Color(0xFF28374F),
    )

/**
 * tv-material 的基础 Typography 只暴露少量槽位；具体字号统一由 LocalTvTypography 提供，
 * 这里仅保留默认值以满足 MaterialTheme 的参数要求。
 */
private val BaseMaterialTypography = Typography()

/**
 * 过扫描安全边距：电视普遍会裁掉边缘 5% 左右，四周留出固定内边距。
 * 具体取值以 1080p（约 48dp）为基准。
 */
object TvInsets {
    /** 尺寸取自 res/values/dimens.xml；4K 设备逻辑分辨率不变，边距无需额外放大。 */
    val horizontal: Dp
        @Composable @ReadOnlyComposable
        get() = dimensionResource(R.dimen.tv_inset_horizontal)

    val vertical: Dp
        @Composable @ReadOnlyComposable
        get() = dimensionResource(R.dimen.tv_inset_vertical)

    val content: PaddingValues
        @Composable @ReadOnlyComposable
        get() = PaddingValues(horizontal = horizontal, vertical = vertical)

    val cardSpacing: Dp
        @Composable @ReadOnlyComposable
        get() = dimensionResource(R.dimen.tv_card_spacing)

    val rowSpacing: Dp
        @Composable @ReadOnlyComposable
        get() = dimensionResource(R.dimen.tv_row_spacing)
}

/** 语义化颜色，正误反馈同时使用颜色 + 图标 + 文案，不只依赖颜色。 */
object TvSemanticColors {
    val correct = Color(0xFF52D273)
    val onCorrect = Color(0xFF04170A)
    val wrong = Color(0xFFFF7A6B)
    val onWrong = Color(0xFF2B0703)
    val focusRing = Color(0xFFFFFFFF)
}

@Composable
fun PhoneticsTvTheme(content: @Composable () -> Unit) {
    ProvideTvTypography {
        MaterialTheme(
            colorScheme = TvColorScheme,
            typography = BaseMaterialTypography,
            content = content,
        )
    }
}
