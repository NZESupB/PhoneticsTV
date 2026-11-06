package com.example.phonetics

import androidx.compose.runtime.staticCompositionLocalOf
import com.example.phonetics.audio.PronunciationPlayer

/** 通过 CompositionLocal 向下传递播放器，避免每个界面层层传参。 */
val LocalPronunciationPlayer =
    staticCompositionLocalOf<PronunciationPlayer> {
        error("PronunciationPlayer 未提供，请在 MainActivity 的 CompositionLocalProvider 中提供")
    }
