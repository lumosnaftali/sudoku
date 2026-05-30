package com.lumos.sudoku.feature.game.domain.usecase

import com.lumos.sudoku.feature.game.domain.model.SudokuCell
import javax.inject.Inject

class GetHintUseCase @Inject constructor() {
    operator fun invoke(cell: SudokuCell): SudokuCell = cell.copy(
        value = cell.solution,
        isWrong = false,
        isHinted = true,
        notes = emptySet()
    )
}
