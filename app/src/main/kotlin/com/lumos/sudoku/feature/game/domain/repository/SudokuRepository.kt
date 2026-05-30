package com.lumos.sudoku.feature.game.domain.repository

import com.lumos.sudoku.core.model.Difficulty
import com.lumos.sudoku.feature.game.domain.model.SudokuCell

interface SudokuRepository {
    fun generatePuzzle(difficulty: Difficulty): List<List<SudokuCell>>
}
