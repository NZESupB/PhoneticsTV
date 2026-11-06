package com.example.phonetics.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.phonetics.ui.browse.BrowseScreen
import com.example.phonetics.ui.detail.PhonemeDetailScreen
import com.example.phonetics.ui.quiz.QuizScreen

/** 应用内的三个目的地。 */
object Routes {
    const val BROWSE = "browse"

    const val DETAIL_ARG = "phonemeId"
    const val DETAIL_PATTERN = "detail/{$DETAIL_ARG}"

    const val QUIZ = "quiz"
    const val QUIZ_PATTERN = "quiz?focus={focus}"
    const val QUIZ_FOCUS_ARG = "focus"

    fun detail(phonemeId: String): String = "detail/$phonemeId"

    fun quiz(focusPhonemeId: String? = null): String =
        if (focusPhonemeId ==
            null
        ) {
            QUIZ
        } else {
            "$QUIZ?$QUIZ_FOCUS_ARG=$focusPhonemeId"
        }
}

/** 导航图：首页（含元音/辅音/练习标签）→ 音标详情 / 练习。 */
@Composable
fun PhoneticsNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.BROWSE) {
        composable(Routes.BROWSE) {
            BrowseScreen(
                onPhonemeClick = { phoneme -> navController.navigate(Routes.detail(phoneme.id)) },
                onPracticeClick = { navController.navigate(Routes.quiz()) },
            )
        }
        composable(
            route = Routes.DETAIL_PATTERN,
            arguments = listOf(navArgument(Routes.DETAIL_ARG) { type = NavType.StringType }),
        ) { entry ->
            PhonemeDetailScreen(
                phonemeId = entry.arguments?.getString(Routes.DETAIL_ARG).orEmpty(),
                onBack = { navController.popBackStack() },
                onPractice = { phonemeId -> navController.navigate(Routes.quiz(phonemeId)) },
            )
        }
        composable(
            route = Routes.QUIZ_PATTERN,
            arguments =
                listOf(
                    navArgument(Routes.QUIZ_FOCUS_ARG) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                ),
        ) { entry ->
            QuizScreen(
                focusPhonemeId = entry.arguments?.getString(Routes.QUIZ_FOCUS_ARG),
                onBack = { navController.popBackStack() },
            )
        }
    }
}
