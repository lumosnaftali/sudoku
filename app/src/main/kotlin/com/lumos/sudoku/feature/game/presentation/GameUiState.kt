package com.lumos.sudoku.feature.game.presentation

import com.lumos.sudoku.core.model.Difficulty
import com.lumos.sudoku.feature.game.domain.model.GameState
import com.lumos.sudoku.feature.game.domain.model.SudokuCell

data class GameUiState(
    val board: List<List<SudokuCell>> = emptyList(),
    val selectedRow: Int = -1,
    val selectedCol: Int = -1,
    val isPencilMode: Boolean = false,
    val mistakes: Int = 0,
    val hintsUsed: Int = 0,
    val gameState: GameState = GameState.Idle,
    val difficulty: Difficulty = Difficulty.EASY,
    val elapsedSeconds: Long = 0,
    val selectedNumber: Int = 0,
    val isInstantFillMode: Boolean = false,
    val conflictCells: Set<Pair<Int, Int>> = emptySet(),
    val numberRemainingCounts: List<Int> = List(9) { 9 }
)
