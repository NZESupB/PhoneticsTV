package com.example.phonetics.ui.quiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.example.phonetics.LocalPronunciationPlayer
import com.example.phonetics.quiz.AnsweredQuestion
import com.example.phonetics.quiz.QuizType
import com.example.phonetics.quiz.QuizUiState
import com.example.phonetics.ui.theme.LocalTvTypography
import com.example.phonetics.ui.theme.TvSemanticColors

/**
 * 结果页：本轮得分与答题正确率，并逐题回顾错题。
 *
 * 错题条目可点击，直接重听该音，让"错了"能立刻变成"再听一次"。
 */
@Composable
fun QuizResult(
    state: QuizUiState.Finished,
    onRestart: () -> Unit,
    onChangeSetup: () -> Unit,
) {
    val typography = LocalTvTypography.current
    val player = LocalPronunciationPlayer.current
    val total = state.answers.size

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "本轮结果",
            fontSize = typography.quizTitle,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "答对 ${state.correctCount} / $total，正确率 ${state.accuracyPercent}%",
            fontSize = typography.quizResultRow,
            color =
                if (state.accuracyPercent >=
                    80
                ) {
                    TvSemanticColors.correct
                } else {
                    MaterialTheme.colorScheme.onBackground
                },
        )

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = onRestart,
                shape = ButtonDefaults.shape(RoundedCornerShape(12.dp)),
            ) {
                Text(
                    "再练一轮",
                    fontSize = typography.quizButton,
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
            }
            Button(
                onClick = onChangeSetup,
                shape = ButtonDefaults.shape(RoundedCornerShape(12.dp)),
            ) {
                Text(
                    "换个题型",
                    fontSize = typography.quizButton,
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
            }
        }

        if (state.wrongAnswers.isEmpty()) {
            Text(
                text = "全部答对，继续保持！",
                fontSize = typography.quizTitle,
                color = TvSemanticColors.correct,
            )
        } else {
            Text(
                text = "错题回顾（按确认键重听该音）",
                fontSize = typography.quizTitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.wrongAnswers, key = { it.question.phoneme.id + it.selectedKey }) { answer ->
                    WrongAnswerRow(
                        answer = answer,
                        type = state.config.type,
                        onClick = { player.playPhoneme(answer.question.phoneme, answer.question.promptWord) },
                    )
                }
            }
        }
    }
}

@Composable
private fun WrongAnswerRow(
    answer: AnsweredQuestion,
    type: QuizType,
    onClick: () -> Unit,
) {
    val typography = LocalTvTypography.current
    val phoneme = answer.question.phoneme
    val correct = answer.question.correctOption
    Button(
        onClick = onClick,
        shape = ButtonDefaults.shape(RoundedCornerShape(10.dp)),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = phoneme.symbol,
                fontSize = typography.quizResultRow,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = "你选了 ${answer.selectedOption.text}，正确答案 ${correct.text}",
                fontSize = typography.quizResultDetail,
            )
            Text(
                text =
                    when (type) {
                        QuizType.LISTEN_PICK_SYMBOL -> "朗读：${answer.question.promptWord.text}"
                        QuizType.SEE_SYMBOL_PICK_WORD -> correct.hint
                    },
                fontSize = typography.quizResultHint,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
