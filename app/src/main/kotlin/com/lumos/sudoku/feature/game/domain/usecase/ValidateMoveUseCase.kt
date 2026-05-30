package com.lumos.sudoku.feature.game.domain.usecase

import com.lumos.sudoku.feature.game.domain.model.SudokuCell
import javax.inject.Inject

class ValidateMoveUseCase @Inject constructor() {
    operator fun invoke(cell: SudokuCell, newValue: Int): SudokuCell {
        val isWrong = newValue != 0 && newValue != cell.solution
        return cell.copy(value = newValue, isWrong = isWrong, notes = emptySet())
    }
}
