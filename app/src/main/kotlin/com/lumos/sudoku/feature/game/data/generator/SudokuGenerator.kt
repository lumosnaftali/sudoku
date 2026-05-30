package com.lumos.sudoku.feature.game.data.generator

import com.lumos.sudoku.core.model.Difficulty

class SudokuGenerator {

    data class GeneratedPuzzle(
        val board: Array<IntArray>,
        val solution: Array<IntArray>
    )

    fun generate(difficulty: Difficulty): GeneratedPuzzle {
        val solution = generateSolvedBoard()
        val board = copyBoard(solution)
        removeCells(board, difficulty)
        return GeneratedPuzzle(board, solution)
    }

    private fun generateSolvedBoard(): Array<IntArray> {
        val board = Array(9) { IntArray(9) }
        fillBoard(board)
        return board
    }

    private fun fillBoard(board: Array<IntArray>): Boolean {
        for (row in 0..8) {
            for (col in 0..8) {
                if (board[row][col] == 0) {
                    val numbers = (1..9).shuffled()
                    for (num in numbers) {
                        if (isValid(board, row, col, num)) {
                            board[row][col] = num
                            if (fillBoard(board)) return true
                            board[row][col] = 0
                        }
                    }
                    return false
                }
            }
        }
        return true
    }

    private fun isValid(board: Array<IntArray>, row: Int, col: Int, num: Int): Boolean {
        for (c in 0..8) if (board[row][c] == num) return false
        for (r in 0..8) if (board[r][col] == num) return false
        val boxRowStart = (row / 3) * 3; val boxColStart = (col / 3) * 3
        for (r in boxRowStart until boxRowStart + 3)
            for (c in boxColStart until boxColStart + 3)
                if (board[r][c] == num) return false
        return true
    }

    private fun countSolutions(board: Array<IntArray>, limit: Int = 2): Int {
        var solutionsCount = 0
        val tempBoard = copyBoard(board)

        fun solveRecursive(r: Int, c: Int): Boolean {
            if (r == 9) { solutionsCount++; return solutionsCount >= limit }
            if (solutionsCount >= limit) return true
            val nextRow: Int; val nextCol: Int
            if (c + 1 == 9) { nextRow = r + 1; nextCol = 0 } else { nextRow = r; nextCol = c + 1 }
            if (tempBoard[r][c] != 0) return solveRecursive(nextRow, nextCol)
            for (num in 1..9) {
                if (isValid(tempBoard, r, c, num)) {
                    tempBoard[r][c] = num
                    if (solveRecursive(nextRow, nextCol)) { tempBoard[r][c] = 0; return true }
                    tempBoard[r][c] = 0
                }
            }
            return false
        }

        solveRecursive(0, 0)
        return solutionsCount
    }

    private fun removeCells(board: Array<IntArray>, difficulty: Difficulty) {
        val targetRevealed = difficulty.revealedCellsRange.random()
        var currentRevealed = 81
        val cells = (0..80).shuffled().toMutableList()
        for (cellIndex in cells) {
            if (currentRevealed <= targetRevealed) break
            val row = cellIndex / 9; val col = cellIndex % 9
            val originalVal = board[row][col]
            if (originalVal != 0) {
                board[row][col] = 0
                if (countSolutions(board) == 1) currentRevealed-- else board[row][col] = originalVal
            }
        }
    }

    private fun copyBoard(source: Array<IntArray>): Array<IntArray> =
        Array(9) { row -> source[row].clone() }
}
