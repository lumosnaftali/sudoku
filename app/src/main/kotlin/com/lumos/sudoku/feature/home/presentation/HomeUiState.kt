package com.lumos.sudoku.feature.home.presentation

import com.lumos.sudoku.core.model.Difficulty

data class HomeUiState(
    val selectedDifficulty: Difficulty = Difficulty.EASY,
    val isDarkTheme: Boolean? = null
)
