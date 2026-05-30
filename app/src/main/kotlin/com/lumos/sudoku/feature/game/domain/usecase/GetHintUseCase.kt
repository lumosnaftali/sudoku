package com.lumos.sudoku.feature.game.domain.usecase

import com.lumos.sudoku.feature.game.domain.model.SudokuCell

class GetHintUseCase {
    operator fun invoke(cell: SudokuCell): SudokuCell = cell.copy(
        value = cell.solution,
        isWrong = false,
        isHinted = true,
        notes = emptySet()
    )
}
