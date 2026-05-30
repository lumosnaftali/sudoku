package com.lumos.sudoku.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lumos.sudoku.core.model.Difficulty
import com.lumos.sudoku.feature.game.presentation.GameScreen
import com.lumos.sudoku.feature.game.presentation.GameViewModel
import com.lumos.sudoku.feature.home.presentation.HomeScreen
import com.lumos.sudoku.feature.home.presentation.HomeViewModel
import com.lumos.sudoku.feature.result.presentation.ResultScreen

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Route.HOME, modifier = modifier) {

        composable(Route.HOME) {
            val viewModel: HomeViewModel = hiltViewModel()
            HomeScreen(
                viewModel = viewModel,
                onStartGame = { difficulty ->
                    navController.navigate(Route.game(difficulty.name))
                }
            )
        }

        composable(
            route = Route.GAME,
            arguments = listOf(navArgument("difficulty") { type = NavType.StringType })
        ) { backStackEntry ->
            val difficultyName = backStackEntry.arguments?.getString("difficulty") ?: Difficulty.EASY.name
            val difficulty = Difficulty.valueOf(difficultyName)
            val viewModel: GameViewModel = hiltViewModel()

            LaunchedEffect(difficulty) { viewModel.initGame(difficulty) }

            GameScreen(
                viewModel = viewModel,
                onBackToHome = {
                    navController.navigate(Route.HOME) { popUpTo(Route.HOME) { inclusive = true } }
                },
                onGameFinished = { isWon, elapsed, mistakes, hints ->
                    navController.navigate(
                        Route.result(isWon, elapsed, mistakes, hints, difficulty.name)
                    ) { popUpTo(Route.HOME) }
                }
            )
        }

        composable(
            route = Route.RESULT,
            arguments = listOf(
                navArgument("isWon") { type = NavType.BoolType },
                navArgument("time") { type = NavType.LongType },
                navArgument("mistakes") { type = NavType.IntType },
                navArgument("hints") { type = NavType.IntType },
                navArgument("difficulty") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val isWon = backStackEntry.arguments?.getBoolean("isWon") ?: true
            val time = backStackEntry.arguments?.getLong("time") ?: 0L
            val mistakes = backStackEntry.arguments?.getInt("mistakes") ?: 0
            val hints = backStackEntry.arguments?.getInt("hints") ?: 0
            val difficultyName = backStackEntry.arguments?.getString("difficulty") ?: Difficulty.EASY.name

            ResultScreen(
                isWon = isWon,
                elapsedSeconds = time,
                mistakes = mistakes,
                hintsUsed = hints,
                onPlayAgain = {
                    navController.navigate(Route.game(difficultyName)) { popUpTo(Route.HOME) }
                },
                onGoHome = {
                    navController.navigate(Route.HOME) { popUpTo(Route.HOME) { inclusive = true } }
                }
            )
        }
    }
}
