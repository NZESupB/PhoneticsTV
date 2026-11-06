package com.example.phonetics.quiz

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** 校验练习流程：作答锁定、判分、进题、结果统计与重开。 */
class QuizViewModelTest {
    private fun viewModel(seed: Int = 42) = QuizViewModel(QuizGenerator(random = Random(seed)))

    private val config =
        QuizConfig(
            type = QuizType.LISTEN_PICK_SYMBOL,
            scope = QuizScope.ALL,
            questionCount = 5,
        )

    @Test
    fun `初始为设置状态`() {
        assertTrue(viewModel().state.value is QuizUiState.Setup)
    }

    @Test
    fun `开始后进入第一题并请求播放音频`() {
        val vm = viewModel()
        vm.start(config)

        val running = vm.state.value as QuizUiState.Running
        assertEquals(0, running.questionIndex)
        assertEquals(5, running.total)
        assertFalse(running.answered)
        assertNotNull("开始练习后应立即请求播放音频", vm.audioRequest.value)
    }

    @Test
    fun `选对时记录正确并锁定选项`() {
        val vm = viewModel()
        vm.start(config)
        val running = vm.state.value as QuizUiState.Running
        val correctKey = running.question.correctOption.key

        vm.select(correctKey)

        val answered = vm.state.value as QuizUiState.Running
        assertTrue(answered.answered)
        assertTrue(answered.isCorrect)
        assertEquals(correctKey, answered.selectedKey)

        // 已作答后再次选择应被忽略，保证每道题只记录一次。
        val anotherKey =
            running.question.options
                .first { !it.isCorrect }
                .key
        vm.select(anotherKey)
        assertEquals(correctKey, (vm.state.value as QuizUiState.Running).selectedKey)
    }

    @Test
    fun `选错时标记错误`() {
        val vm = viewModel()
        vm.start(config)
        val running = vm.state.value as QuizUiState.Running
        val wrongKey =
            running.question.options
                .first { !it.isCorrect }
                .key

        vm.select(wrongKey)

        val answered = vm.state.value as QuizUiState.Running
        assertTrue(answered.answered)
        assertFalse(answered.isCorrect)
    }

    @Test
    fun `未作答时无法进入下一题`() {
        val vm = viewModel()
        vm.start(config)
        val before = vm.state.value as QuizUiState.Running

        vm.next()

        assertEquals(before.questionIndex, (vm.state.value as QuizUiState.Running).questionIndex)
    }

    @Test
    fun `答完后进入下一题并请求新的播放`() {
        val vm = viewModel()
        vm.start(config)
        val first = vm.state.value as QuizUiState.Running
        vm.select(first.question.correctOption.key)
        val firstRequestId = vm.audioRequest.value?.id

        vm.next()

        val second = vm.state.value as QuizUiState.Running
        assertEquals(1, second.questionIndex)
        assertFalse(second.answered)
        assertTrue("换题后应重新请求播放", (vm.audioRequest.value?.id ?: 0) > (firstRequestId ?: 0))
    }

    @Test
    fun `答完最后一题后进入结果页并统计错题`() {
        val vm = viewModel()
        vm.start(config)
        var answeredCount = 0
        while (true) {
            val running = vm.state.value as? QuizUiState.Running ?: break
            // 第一题故意答错，其余答对。
            val option =
                if (answeredCount == 0) {
                    running.question.options.first { !it.isCorrect }
                } else {
                    running.question.correctOption
                }
            vm.select(option.key)
            vm.next()
            answeredCount += 1
        }

        val finished = vm.state.value as QuizUiState.Finished
        assertEquals(5, finished.answers.size)
        assertEquals(4, finished.correctCount)
        assertEquals(80, finished.accuracyPercent)
        assertEquals(1, finished.wrongAnswers.size)
        assertFalse(finished.wrongAnswers.first().isCorrect)
    }

    @Test
    fun `重开一轮会清空上轮成绩`() {
        val vm = viewModel()
        vm.start(config)
        val running = vm.state.value as QuizUiState.Running
        vm.select(running.question.correctOption.key)
        vm.next()

        vm.restart()

        val restarted = vm.state.value as QuizUiState.Running
        assertEquals(0, restarted.questionIndex)
        assertFalse(restarted.answered)
    }

    @Test
    fun `回到设置页会重置状态`() {
        val vm = viewModel()
        vm.start(config)
        vm.backToSetup()

        assertTrue(vm.state.value is QuizUiState.Setup)
    }

    @Test
    fun `重播会再次发出播放请求`() {
        val vm = viewModel()
        vm.start(config)
        val firstId = vm.audioRequest.value?.id

        vm.replayAudio()

        assertTrue((vm.audioRequest.value?.id ?: 0) > (firstId ?: 0))
    }

    @Test
    fun `消费播放请求后不会重复播放`() {
        val vm = viewModel()
        vm.start(config)
        val request = requireNotNull(vm.audioRequest.value)

        vm.consumeAudioRequest(request.id)

        assertEquals(null, vm.audioRequest.value)
    }

    @Test
    fun `音标池不足以凑出选项时显式失败`() {
        val error =
            runCatching {
                QuizGenerator(
                    catalog =
                        com.example.phonetics.data.PhonemeCatalog.all
                            .take(2),
                )
            }.exceptionOrNull()
        assertTrue("音标池过小应当报错而不是静默出错题", error is IllegalArgumentException)
    }
}
