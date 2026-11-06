package com.example.phonetics.ui.quiz

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.example.phonetics.LocalPronunciationPlayer
import com.example.phonetics.data.PhonemeCatalog
import com.example.phonetics.data.PhonemeCategory
import com.example.phonetics.quiz.QuizConfig
import com.example.phonetics.quiz.QuizGenerator
import com.example.phonetics.quiz.QuizScope
import com.example.phonetics.quiz.QuizType
import com.example.phonetics.quiz.QuizUiState
import com.example.phonetics.quiz.QuizViewModel
import com.example.phonetics.ui.detail.PlaybackStatusLine
import com.example.phonetics.ui.theme.LocalTvTypography
import com.example.phonetics.ui.theme.TvInsets
import com.example.phonetics.ui.theme.TvSemanticColors
import kotlin.random.Random

/**
 * 练习入口：按状态机渲染设置页、答题页、结果页。
 *
 * [focusPhonemeId] 来自详情页"练这个音"：设置页会预先选中该音标所属的大类。
 */
@Composable
fun QuizScreen(
    focusPhonemeId: String?,
    onBack: () -> Unit,
) {
    val seed = remember { Random.Default.nextLong() }
    val viewModel: QuizViewModel =
        viewModel(
            factory = QuizViewModel.factory(QuizGenerator(random = Random(seed))),
        )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val audioRequest by viewModel.audioRequest.collectAsStateWithLifecycle()
    val player = LocalPronunciationPlayer.current
    val status by player.status.collectAsStateWithLifecycle()

    // 状态机只发出播放请求，实际播放由界面驱动，便于替换播放器实现。
    LaunchedEffect(audioRequest?.id) {
        val request = audioRequest ?: return@LaunchedEffect
        player.playPhoneme(request.phoneme, request.question.promptWord)
        viewModel.consumeAudioRequest(request.id)
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(TvInsets.content)
                .onPreviewKeyEvent { event ->
                    // 遥控器播放/暂停键重听，避免用户把焦点移回按钮。
                    val replayKey = event.key == Key.MediaPlayPause || event.key == Key.MediaPlay
                    if (event.type == KeyEventType.KeyUp && replayKey) {
                        viewModel.replayAudio()
                        true
                    } else {
                        false
                    }
                },
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            when (val current = state) {
                QuizUiState.Setup ->
                    QuizSetup(
                        initialScope = initialScopeFor(focusPhonemeId),
                        onStart = viewModel::start,
                        onBack = onBack,
                    )
                is QuizUiState.Running ->
                    QuizRunning(
                        state = current,
                        onSelect = viewModel::select,
                        onNext = viewModel::next,
                        onReplay = viewModel::replayAudio,
                    )
                is QuizUiState.Finished ->
                    QuizResult(
                        state = current,
                        onRestart = viewModel::restart,
                        onChangeSetup = viewModel::backToSetup,
                    )
                is QuizUiState.Failed ->
                    QuizFailed(
                        message = current.message,
                        onRetry = { viewModel.start(current.config) },
                    )
            }
        }
        // 结果页没有待作答内容，底部不再提示"作答"。
        if (state !is QuizUiState.Finished) {
            PlaybackStatusLine(status = status, hint = "按下确认键作答，播放键可重听")
        }
    }
}

/** 从详情页"练这个音"进入时，按该音标所属大类预置练习范围。 */
private fun initialScopeFor(focusPhonemeId: String?): QuizScope? =
    focusPhonemeId
        ?.let(PhonemeCatalog::findById)
        ?.let { phoneme ->
            if (phoneme.category == PhonemeCategory.VOWEL) QuizScope.VOWELS else QuizScope.CONSONANTS
        }

/** 设置页：选题型与范围后开始。 */
@Composable
private fun QuizSetup(
    initialScope: QuizScope?,
    onStart: (QuizConfig) -> Unit,
    onBack: () -> Unit,
) {
    val typography = LocalTvTypography.current
    var type by rememberSaveable { mutableStateOf(QuizType.LISTEN_PICK_SYMBOL) }
    var scope by rememberSaveable { mutableStateOf(initialScope ?: QuizScope.ALL) }

    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Text(
            text = "选择练习方式",
            fontSize = typography.quizTitle,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        OptionGroup(title = "题型") {
            QuizType.entries.forEach { candidate ->
                ChoiceButton(
                    label = candidate.label,
                    selected = type == candidate,
                    onClick = { type = candidate },
                )
            }
        }

        OptionGroup(title = "范围") {
            QuizScope.entries.forEach { candidate ->
                ChoiceButton(
                    label = candidate.label,
                    selected = scope == candidate,
                    onClick = { scope = candidate },
                )
            }
        }

        Text(
            text = type.instruction,
            fontSize = typography.quizChoice,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = { onStart(QuizConfig(type = type, scope = scope)) },
                shape = ButtonDefaults.shape(RoundedCornerShape(12.dp)),
            ) {
                Text(
                    "开始（10 题）",
                    fontSize = typography.quizButton,
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
            }
            Button(
                onClick = onBack,
                shape = ButtonDefaults.shape(RoundedCornerShape(12.dp)),
            ) {
                Text(
                    "返回列表",
                    fontSize = typography.quizButton,
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
            }
        }
    }
}

@Composable
private fun OptionGroup(
    title: String,
    content: @Composable () -> Unit,
) {
    val typography = LocalTvTypography.current
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title,
            fontSize = typography.quizChoice,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { content() }
    }
}

/** 设置项按钮：选中态由容器色 + 边框共同表达，不依赖颜色单一通道。 */
@Composable
private fun ChoiceButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val typography = LocalTvTypography.current
    Button(
        onClick = onClick,
        shape = ButtonDefaults.shape(RoundedCornerShape(12.dp)),
        border =
            ButtonDefaults.border(
                border = Border(BorderStroke(1.dp, MaterialTheme.colorScheme.borderVariant)),
                focusedBorder = Border(BorderStroke(3.dp, TvSemanticColors.focusRing)),
            ),
        colors =
            ButtonDefaults.colors(
                containerColor =
                    if (selected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                contentColor =
                    if (selected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
            ),
    ) {
        Text(
            text = if (selected) "✓ $label" else label,
            fontSize = typography.quizChoice,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun QuizFailed(
    message: String,
    onRetry: () -> Unit,
) {
    val typography = LocalTvTypography.current
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            "出题失败：$message",
            fontSize = typography.quizFeedback,
            color = MaterialTheme.colorScheme.tertiary,
        )
        Button(onClick = onRetry) { Text("重试", fontSize = typography.quizChoice) }
    }
}
