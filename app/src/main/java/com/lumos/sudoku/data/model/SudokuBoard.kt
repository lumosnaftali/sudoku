package com.lumos.sudoku.data.model

data class SudokuBoard(
    val cells: List<List<SudokuCell>>
) {
    fun getCell(row: Int, col: Int): SudokuCell = cells[row][col]
}
