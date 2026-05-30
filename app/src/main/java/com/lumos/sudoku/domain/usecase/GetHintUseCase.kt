package com.lumos.sudoku.domain.usecase

import com.lumos.sudoku.data.model.SudokuCell
import javax.inject.Inject

class GetHintUseCase @Inject constructor() {
    operator fun invoke(cell: SudokuCell): SudokuCell {
        return cell.copy(
            value = cell.solution,
            isWrong = false,
            isHinted = true,
            notes = emptySet()
        )
    }
}
