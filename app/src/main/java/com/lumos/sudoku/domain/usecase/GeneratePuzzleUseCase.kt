package com.lumos.sudoku.domain.usecase

import com.lumos.sudoku.data.model.Difficulty
import com.lumos.sudoku.data.model.SudokuCell
import com.lumos.sudoku.data.repository.SudokuRepository
import javax.inject.Inject

class GeneratePuzzleUseCase @Inject constructor(
    private val repository: SudokuRepository
) {
    operator fun invoke(difficulty: Difficulty): List<List<SudokuCell>> {
        return repository.generatePuzzle(difficulty)
    }
}
