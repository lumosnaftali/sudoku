package com.lumos.sudoku.feature.game.domain.usecase

import com.lumos.sudoku.feature.game.domain.model.SudokuCell
import javax.inject.Inject

class CheckCompletionUseCase @Inject constructor() {
    operator fun invoke(board: List<List<SudokuCell>>): Boolean {
        for (row in board) for (cell in row) if (cell.value != cell.solution) return false
        return true
    }
}
