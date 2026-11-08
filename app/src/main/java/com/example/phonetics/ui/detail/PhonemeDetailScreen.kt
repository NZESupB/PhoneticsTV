package com.example.phonetics.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.example.phonetics.LocalPronunciationPlayer
import com.example.phonetics.R
import com.example.phonetics.audio.PlaybackSource
import com.example.phonetics.audio.PlayerStatus
import com.example.phonetics.data.ExampleWord
import com.example.phonetics.data.PhonemeCatalog
import com.example.phonetics.ui.quiz.requestFocusSafely
import com.example.phonetics.ui.theme.LocalTvTypography
import com.example.phonetics.ui.theme.TvInsets

/**
 * 音标详情：大字号音标、发音要领、逐个示例词的朗读按钮，以及"再听一遍 / 练这个音"。
 *
 * 进入页面自动播放一次该音标发音，焦点默认落在"再听一遍"上。
 */
@Composable
fun PhonemeDetailScreen(
    phonemeId: String,
    onBack: () -> Unit,
    onPractice: (String) -> Unit,
) {
    val typography = LocalTvTypography.current
    val phoneme = PhonemeCatalog.findById(phonemeId)
    if (phoneme == null) {
        MissingPhoneme(onBack)
        return
    }
    val player = LocalPronunciationPlayer.current
    val status by player.status.collectAsStateWithLifecycle()
    val firstButton = remember { FocusRequester() }

    LaunchedEffect(phoneme.id) {
        player.playPhoneme(phoneme, phoneme.examples.first())
        firstButton.requestFocusSafely()
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(TvInsets.content),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = "${phoneme.group.label} · 音标 /${phoneme.symbol}/",
            fontSize = typography.detailHeader,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.detail_column_spacing)),
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(0.42f),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = phoneme.symbol,
                    fontSize = typography.detailSymbol,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = phoneme.tip,
                    fontSize = typography.detailTip,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "示例单词",
                    fontSize = typography.detailSection,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                phoneme.examples.forEach { word ->
                    ExampleWordRow(
                        word = word,
                        onClick = { player.playWord(word) },
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = { player.playPhoneme(phoneme, phoneme.examples.first()) },
                        shape = ButtonDefaults.shape(RoundedCornerShape(12.dp)),
                        modifier = Modifier.focusRequester(firstButton),
                    ) {
                        Text(
                            "再听一遍",
                            fontSize = typography.detailButton,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 10.dp),
                        )
                    }
                    Button(
                        onClick = { onPractice(phoneme.id) },
                        shape = ButtonDefaults.shape(RoundedCornerShape(12.dp)),
                    ) {
                        Text(
                            "练这个音",
                            fontSize = typography.detailButton,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 10.dp),
                        )
                    }
                }

                PlaybackStatusLine(status = status, hint = "返回键回到列表")
            }
        }
    }
}

@Composable
private fun ExampleWordRow(
    word: ExampleWord,
    onClick: () -> Unit,
) {
    val typography = LocalTvTypography.current
    Button(
        onClick = onClick,
        shape = ButtonDefaults.shape(RoundedCornerShape(12.dp)),
    ) {
        Row(
            modifier = Modifier.padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = word.text,
                fontSize = typography.detailWord,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = word.transcription,
                fontSize = typography.detailTranscription,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
            Text(
                text = word.meaning,
                fontSize = typography.detailTranscription,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "朗读",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun MissingPhoneme(onBack: () -> Unit) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(TvInsets.content),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("找不到这个音标", fontSize = 26.sp, color = MaterialTheme.colorScheme.onBackground)
        Button(onClick = onBack) { Text("返回列表", fontSize = 18.sp) }
    }
}

/** 播放状态提示：把"正在朗读哪个词"或"为什么没声音"如实告诉用户。 */
@Composable
internal fun PlaybackStatusLine(
    status: PlayerStatus,
    hint: String,
    modifier: Modifier = Modifier,
) {
    val typography = LocalTvTypography.current
    val text =
        when (status) {
            PlayerStatus.Idle -> hint.ifBlank { "按确认键试听" }
            is PlayerStatus.Playing ->
                when (status.source) {
                    PlaybackSource.PHONEME_RECORDING -> "正在播放音标 ${status.label}"
                    PlaybackSource.WORD_RECORDING -> "正在播放单词 ${status.label}"
                    PlaybackSource.ONLINE -> "正在播放在线发音：${status.label}"
                    PlaybackSource.SPEECH -> "设备朗读示例词：${status.label}"
                }
            is PlayerStatus.Loading -> "正在获取在线发音：${status.label}…"
            is PlayerStatus.Unavailable -> status.message
        }
    val color =
        when (status) {
            is PlayerStatus.Unavailable -> MaterialTheme.colorScheme.tertiary
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }
    Text(
        text = text,
        fontSize = typography.detailStatus,
        color = color,
        modifier = modifier,
    )
}
