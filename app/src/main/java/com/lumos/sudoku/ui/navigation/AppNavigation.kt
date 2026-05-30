package com.lumos.sudoku.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lumos.sudoku.data.model.Difficulty
import com.lumos.sudoku.ui.screen.game.GameScreen
import com.lumos.sudoku.ui.screen.game.GameViewModel
import com.lumos.sudoku.ui.screen.home.HomeScreen
import com.lumos.sudoku.ui.screen.home.HomeViewModel
import com.lumos.sudoku.ui.screen.result.ResultScreen

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier
    ) {
        composable("home") {
            val homeViewModel: HomeViewModel = hiltViewModel()
            HomeScreen(
                viewModel = homeViewModel,
                onStartGame = { difficulty ->
                    navController.navigate("game/${difficulty.name}")
                }
            )
        }

        composable(
            route = "game/{difficulty}",
            arguments = listOf(
                navArgument("difficulty") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val difficultyName = backStackEntry.arguments?.getString("difficulty") ?: Difficulty.EASY.name
            val difficulty = Difficulty.valueOf(difficultyName)
            val gameViewModel: GameViewModel = hiltViewModel()
            
            androidx.compose.runtime.LaunchedEffect(difficulty) {
                gameViewModel.initGame(difficulty)
            }

            GameScreen(
                viewModel = gameViewModel,
                onBackToHome = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onGameFinished = { isWon, elapsedSeconds, mistakes, hintsUsed ->
                    navController.navigate("result/$isWon/$elapsedSeconds/$mistakes/$hintsUsed/${difficulty.name}") {
                        popUpTo("home")
                    }
                }
            )
        }

        composable(
            route = "result/{isWon}/{time}/{mistakes}/{hints}/{difficulty}",
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
                    navController.navigate("game/$difficultyName") {
                        popUpTo("home")
                    }
                },
                onGoHome = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
    }
}
