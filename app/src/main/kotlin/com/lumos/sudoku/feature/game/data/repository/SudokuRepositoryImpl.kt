package com.lumos.sudoku.feature.game.data.repository

import com.lumos.sudoku.core.model.Difficulty
import com.lumos.sudoku.feature.game.data.generator.SudokuGenerator
import com.lumos.sudoku.feature.game.domain.model.SudokuCell
import com.lumos.sudoku.feature.game.domain.repository.SudokuRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SudokuRepositoryImpl @Inject constructor(
    private val generator: SudokuGenerator
) : SudokuRepository {

    override fun generatePuzzle(difficulty: Difficulty): List<List<SudokuCell>> {
        val puzzle = generator.generate(difficulty)
        return List(9) { r ->
            List(9) { c ->
                val initVal = puzzle.board[r][c]
                val solVal = puzzle.solution[r][c]
                SudokuCell(
                    row = r,
                    col = c,
                    value = initVal,
                    solution = solVal,
                    isGiven = initVal != 0,
                    isWrong = false,
                    isHinted = false,
                    notes = emptySet()
                )
            }
        }
    }
}
