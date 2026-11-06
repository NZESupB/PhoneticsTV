package com.example.phonetics

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.tv.material3.MaterialTheme
import com.example.phonetics.audio.AssetOrTtsPlayer
import com.example.phonetics.audio.PronunciationPlayer
import com.example.phonetics.ui.navigation.PhoneticsNavHost
import com.example.phonetics.ui.theme.PhoneticsTvTheme

/** 简单的手动依赖容器：项目规模小，不引入 DI 框架。 */
class PhoneticsApp(
    context: android.content.Context,
) {
    val player: PronunciationPlayer = AssetOrTtsPlayer(context)
}

class MainActivity : ComponentActivity() {
    private val app by lazy { PhoneticsApp(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PhoneticsTvTheme {
                CompositionLocalProvider(LocalPronunciationPlayer provides app.player) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background),
                    ) {
                        PhoneticsNavHost()
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // 离开前台立即停止发音，避免 TV 场景下声音残留。
        app.player.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        app.player.shutdown()
    }
}
