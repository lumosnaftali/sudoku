package com.lumos.sudoku.feature.game.domain.usecase

import com.lumos.sudoku.core.model.Difficulty
import com.lumos.sudoku.feature.game.domain.model.SudokuCell
import com.lumos.sudoku.feature.game.domain.repository.SudokuRepository
import javax.inject.Inject

class GeneratePuzzleUseCase @Inject constructor(
    private val repository: SudokuRepository
) {
    operator fun invoke(difficulty: Difficulty): List<List<SudokuCell>> =
        repository.generatePuzzle(difficulty)
}
