package com.example.phonetics.ui.browse

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Tab
import androidx.tv.material3.TabDefaults
import androidx.tv.material3.TabRow
import androidx.tv.material3.Text
import com.example.phonetics.R
import com.example.phonetics.data.Phoneme
import com.example.phonetics.data.PhonemeCatalog
import com.example.phonetics.data.PhonemeCategory
import com.example.phonetics.data.PhonemeGroup
import com.example.phonetics.ui.quiz.requestFocusSafely
import com.example.phonetics.ui.theme.LocalTvTypography
import com.example.phonetics.ui.theme.TvInsets

/** 预览头固定高度：两行要领 + 一行示例至少需要的高度。 */
private val PreviewHeaderHeight: Dp
    @Composable @ReadOnlyComposable
    get() = dimensionResource(R.dimen.browse_preview_height)

/** 选中标签的浅色药丸底上的文字色。 */
private val DarkOnPill = Color(0xFF0B1220)

/** 首页标签页。练习作为第三个标签，让遥控器始终只需横向移动。 */
private enum class BrowseTab(
    val label: String,
    val category: PhonemeCategory?,
) {
    VOWELS("元音 · 20", PhonemeCategory.VOWEL),
    CONSONANTS("辅音 · 28", PhonemeCategory.CONSONANT),
    QUIZ("练习", null),
}

/**
 * 首页：顶部标签 + Leanback 式分组行。
 *
 * 焦点落在卡片上时，顶部预览面板同步显示该音标的发音要领与示例词，
 * 对应 BrowseFragment 的 header 区，避免用户先进入详情才知道内容。
 */
@Composable
fun BrowseScreen(
    onPhonemeClick: (Phoneme) -> Unit,
    onPracticeClick: () -> Unit,
) {
    val typography = LocalTvTypography.current
    // 进入首页主动给出焦点：否则遥控器第一下方向键无反应，画面上也看不到焦点。
    // 首次组合时 Tab 尚未附着到窗口，必须等一帧以上再请求，故用带延迟的安全请求。
    val firstTabFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { firstTabFocus.requestFocusSafely(frames = 3) }

    var selectedTab by rememberSaveable { mutableStateOf(BrowseTab.VOWELS) }
    // 预览按大类分别记录，切换标签后不会残留另一个大类的音标。
    var previewByCategory by remember { mutableStateOf<Map<PhonemeCategory, Phoneme>>(emptyMap()) }
    val previewPhoneme = selectedTab.category?.let(previewByCategory::get)

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(TvInsets.content),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "英语音标 · 英式 DJ 48 音",
                fontSize = typography.browseTitle,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        TabRow(selectedTabIndex = selectedTab.ordinal) {
            BrowseTab.entries.forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onFocus = { selectedTab = tab },
                    onClick = { selectedTab = tab },
                    // 默认 Tab 配色在本深色主题下焦点几乎不可见，这里显式给出高对比的焦点/选中配色。
                    // 选中/聚焦一律使用「主色药丸底 + 深色文字」，
                    // 这样无论浅色药丸是否被主题渲染出来，文字对比度都足够。
                    colors =
                        TabDefaults.pillIndicatorTabColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            focusedContentColor = DarkOnPill,
                            selectedContentColor = DarkOnPill,
                            focusedSelectedContentColor = DarkOnPill,
                        ),
                    modifier =
                        if (tab == BrowseTab.entries.first()) {
                            Modifier.focusRequester(firstTabFocus)
                        } else {
                            Modifier
                        },
                ) {
                    Text(
                        text = tab.label,
                        fontSize = typography.browseTab,
                        modifier = Modifier.padding(horizontal = 8.dp),
                    )
                }
            }
        }

        when (selectedTab) {
            BrowseTab.VOWELS, BrowseTab.CONSONANTS -> {
                val category = requireNotNull(selectedTab.category)
                PhonemeRows(
                    category = category,
                    previewPhoneme = previewPhoneme,
                    onPreview = { previewByCategory = previewByCategory + (category to it) },
                    onPhonemeClick = onPhonemeClick,
                )
            }
            BrowseTab.QUIZ -> QuizEntry(onPracticeClick = onPracticeClick)
        }
    }
}

@Composable
private fun PhonemeRows(
    category: PhonemeCategory,
    previewPhoneme: Phoneme?,
    onPreview: (Phoneme) -> Unit,
    onPhonemeClick: (Phoneme) -> Unit,
) {
    val groups = PhonemeCatalog.groupedByCategory(category)
    val highlighted = previewPhoneme?.takeIf { it.category == category } ?: groups.first().second.first()
    // 预览头固定在上方：LazyColumn 会把获得焦点的行滚到顶部，若把头部放进列表会被裁切。
    Column(verticalArrangement = Arrangement.spacedBy(TvInsets.rowSpacing)) {
        PreviewHeader(phoneme = highlighted)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(TvInsets.rowSpacing)) {
            items(groups, key = { it.first.name }) { (group, phonemes) ->
                PhonemeRow(
                    group = group,
                    phonemes = phonemes,
                    onPreview = onPreview,
                    onPhonemeClick = onPhonemeClick,
                )
            }
        }
    }
}

/** 顶部预览区：当前焦点音标的要领与示例，纯展示，不参与焦点。固定高度以免换行时下方列表跳动。 */
@Composable
private fun PreviewHeader(phoneme: Phoneme) {
    val typography = LocalTvTypography.current
    Column(
        modifier = Modifier.height(PreviewHeaderHeight),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = "${phoneme.group.label} · /${phoneme.symbol}/",
            fontSize = typography.browsePreviewTitle,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = phoneme.tip,
            fontSize = typography.browsePreviewTip,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = phoneme.examples.joinToString("   ") { "${it.text} ${it.transcription} ${it.meaning}" },
            fontSize = typography.browsePreviewExamples,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PhonemeRow(
    group: PhonemeGroup,
    phonemes: List<Phoneme>,
    onPreview: (Phoneme) -> Unit,
    onPhonemeClick: (Phoneme) -> Unit,
) {
    val typography = LocalTvTypography.current
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = group.label,
            fontSize = typography.browseGroupLabel,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(TvInsets.cardSpacing),
            contentPadding = PaddingValues(end = TvInsets.horizontal),
        ) {
            items(phonemes, key = { it.id }) { phoneme ->
                PhonemeCard(
                    phoneme = phoneme,
                    onFocus = { onPreview(phoneme) },
                    onClick = { onPhonemeClick(phoneme) },
                )
            }
        }
    }
}

/** 音标卡片：大字号音标 + 首个示例词，焦点时由 tv-material Card 放大并加边框。 */
@Composable
private fun PhonemeCard(
    phoneme: Phoneme,
    onFocus: () -> Unit,
    onClick: () -> Unit,
) {
    val typography = LocalTvTypography.current
    Card(
        onClick = onClick,
        shape = CardDefaults.shape(RoundedCornerShape(16.dp)),
        colors =
            CardDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
        scale = CardDefaults.scale(focusedScale = 1.08f),
        border =
            CardDefaults.border(
                border =
                    androidx.tv.material3.Border(
                        border = androidx.compose.foundation.BorderStroke(0.dp, Color.Transparent),
                    ),
                focusedBorder =
                    androidx.tv.material3.Border(
                        border = androidx.compose.foundation.BorderStroke(3.dp, MaterialTheme.colorScheme.primary),
                    ),
            ),
        modifier =
            Modifier
                .width(dimensionResource(R.dimen.browse_card_width))
                .onFocusChanged { if (it.isFocused) onFocus() },
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = phoneme.symbol,
                fontSize = typography.browseCardSymbol,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = phoneme.examples.first().text,
                fontSize = typography.browseCardWord,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** 练习标签页内容：直接进入练习（也保留一个可点击的大按钮）。 */
@Composable
private fun QuizEntry(onPracticeClick: () -> Unit) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "听音辨标与看标选词，每轮 10 题，出错会给出正确读音与释义。",
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onBackground,
        )
        androidx.tv.material3.Button(
            onClick = onPracticeClick,
            shape =
                androidx.tv.material3.ButtonDefaults
                    .shape(RoundedCornerShape(12.dp)),
        ) {
            Text(text = "开始练习", fontSize = 20.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
        }
    }
}
