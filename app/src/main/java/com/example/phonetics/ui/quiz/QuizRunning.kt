package com.example.phonetics.ui.quiz

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.example.phonetics.R
import com.example.phonetics.quiz.QuizOption
import com.example.phonetics.quiz.QuizType
import com.example.phonetics.quiz.QuizUiState
import com.example.phonetics.ui.theme.LocalTvTypography
import com.example.phonetics.ui.theme.TvSemanticColors
import kotlinx.coroutines.yield

/**
 * 答题页。
 *
 * 遥控器交互：
 * - 方向键在 2×2 选项网格中移动；
 * - 确认键作答，作答后选项锁定并展示正误；
 * - 作答后焦点移到"下一题"，避免误触重答；
 * - 播放键 / "再听一遍"按钮重播题目音频。
 */
@Composable
fun QuizRunning(
    state: QuizUiState.Running,
    onSelect: (String) -> Unit,
    onNext: () -> Unit,
    onReplay: () -> Unit,
) {
    val typography = LocalTvTypography.current
    val firstOptionFocus = remember { FocusRequester() }
    val nextButtonFocus = remember { FocusRequester() }

    // 新题目出现时把焦点放到第一个选项，遥控器用户无需再寻找焦点。
    // requestFocus 在目标尚未附着时会抛 IllegalStateException，用安全封装避免崩溃。
    LaunchedEffect(state.questionIndex) {
        firstOptionFocus.requestFocusSafely()
    }
    // 作答后焦点移到"下一题"。
    LaunchedEffect(state.selectedKey) {
        if (state.selectedKey != null) nextButtonFocus.requestFocusSafely()
    }

    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${state.config.type.label} · ${state.config.scope.label}",
                fontSize = typography.quizMeta,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "第 ${state.questionIndex + 1} / ${state.total} 题",
                fontSize = typography.quizMeta,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        QuestionPrompt(state = state, onReplay = onReplay)

        // 2×2 选项网格
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            state.question.options.chunked(2).forEachIndexed { rowIndex, rowOptions ->
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    rowOptions.forEachIndexed { columnIndex, option ->
                        val index = rowIndex * 2 + columnIndex
                        key(option.key) {
                            OptionButton(
                                option = option,
                                index = index,
                                answered = state.answered,
                                selected = state.selectedKey == option.key,
                                onSelect = { onSelect(option.key) },
                                modifier =
                                    if (index == 0) {
                                        Modifier.focusRequester(firstOptionFocus)
                                    } else {
                                        Modifier
                                    },
                            )
                        }
                    }
                }
            }
        }

        if (state.answered) {
            FeedbackLine(state = state, onNext = onNext, nextFocus = nextButtonFocus)
        }
    }
}

/** 题面：看标选词直接显示音标；听音辨标显示喇叭提示和一个"再听一遍"按钮。 */
@Composable
private fun QuestionPrompt(
    state: QuizUiState.Running,
    onReplay: () -> Unit,
) {
    val typography = LocalTvTypography.current
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        when (state.config.type) {
            QuizType.SEE_SYMBOL_PICK_WORD ->
                Text(
                    text = state.question.phoneme.symbol,
                    fontSize = typography.quizPromptSymbol,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            QuizType.LISTEN_PICK_SYMBOL -> {
                Text(
                    text = "听发音，选出对应的音标",
                    fontSize = typography.quizPrompt,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = onReplay,
                        shape = ButtonDefaults.shape(RoundedCornerShape(12.dp)),
                    ) {
                        Text(
                            "再听一遍",
                            fontSize = typography.quizChoice,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 10.dp),
                        )
                    }
                    Text(
                        text = "提示：遥控器播放键也能重听",
                        fontSize = typography.quizStatus,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.CenterVertically),
                    )
                }
                // 作答后揭晓正确答案词，帮助用户建立"声音—单词"的联系。
                if (state.answered) {
                    Text(
                        text = "本题朗读：${state.question.promptWord.text} ${state.question.promptWord.transcription}",
                        fontSize = typography.quizChoice,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

/** 选项按钮：作答前可点击；作答后用容器色 + 图标 + 文案三重表达正误。 */
@Composable
private fun OptionButton(
    option: QuizOption,
    index: Int,
    answered: Boolean,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val typography = LocalTvTypography.current
    val correct = answered && option.isCorrect
    val wrong = answered && selected && !option.isCorrect
    val container =
        when {
            correct -> TvSemanticColors.correct
            wrong -> TvSemanticColors.wrong
            else -> MaterialTheme.colorScheme.surface
        }
    val content =
        when {
            correct -> TvSemanticColors.onCorrect
            wrong -> TvSemanticColors.onWrong
            else -> MaterialTheme.colorScheme.onSurface
        }
    val marker =
        when {
            correct -> "✔ "
            wrong -> "✘ "
            else -> "${('A' + index)}  "
        }

    Button(
        onClick = onSelect,
        enabled = !answered,
        shape = ButtonDefaults.shape(RoundedCornerShape(14.dp)),
        colors =
            ButtonDefaults.colors(
                containerColor = container,
                contentColor = content,
                focusedContainerColor = container,
                focusedContentColor = content,
                disabledContainerColor = container,
                disabledContentColor = content,
            ),
        border =
            ButtonDefaults.border(
                border = Border(BorderStroke(1.dp, MaterialTheme.colorScheme.borderVariant)),
                focusedBorder = Border(BorderStroke(3.dp, TvSemanticColors.focusRing)),
            ),
        modifier =
            modifier
                .width(dimensionResource(R.dimen.quiz_option_width))
                .heightIn(min = dimensionResource(R.dimen.quiz_option_min_height)),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = marker + option.text,
                fontSize = typography.quizOption,
                fontWeight = FontWeight.Medium,
                color = content,
            )
            if (correct || wrong) {
                // 已作答的选项处于 disabled 状态，文字会继承半透明前景；显式给实色保证可读。
                Text(
                    text = option.hint,
                    fontSize = typography.quizOptionHint,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = content,
                )
            }
        }
    }
}

/** 作答反馈：结果文案 + "下一题/看结果"按钮。 */
@Composable
private fun FeedbackLine(
    state: QuizUiState.Running,
    onNext: () -> Unit,
    nextFocus: FocusRequester,
) {
    val typography = LocalTvTypography.current
    val isLast = state.questionIndex + 1 >= state.total
    Row(
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = if (state.isCorrect) "✔ 回答正确" else "✘ 回答错误，正确答案：${state.question.correctOption.text}",
            fontSize = typography.quizFeedback,
            fontWeight = FontWeight.SemiBold,
            color = if (state.isCorrect) TvSemanticColors.correct else TvSemanticColors.wrong,
        )
        var focused by remember(state.questionIndex) { mutableStateOf(false) }
        Button(
            onClick = onNext,
            shape = ButtonDefaults.shape(RoundedCornerShape(12.dp)),
            modifier =
                Modifier
                    .focusRequester(nextFocus)
                    .onFocusChanged { focused = it.isFocused },
        ) {
            Text(
                text = if (isLast) "查看结果" else "下一题",
                fontSize = typography.quizButton,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
        }
        if (focused) {
            Text(
                text = "按确认键继续",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * 焦点请求的安全封装。
 *
 * 首次组合时目标往往还没附着到窗口，此时 requestFocus 会抛 IllegalStateException 且焦点静默丢失。
 * 这里先让出 [frames] 帧等布局完成再请求；仍失败则忽略（用户可用方向键自行移动焦点），不会崩溃。
 */
internal suspend fun FocusRequester.requestFocusSafely(frames: Int = 1) {
    repeat(frames.coerceAtLeast(1)) { yield() }
    runCatching { requestFocus() }
}
