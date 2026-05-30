package com.lumos.sudoku.data.model

data class SudokuCell(
    val row: Int,
    val col: Int,
    val value: Int,          // 0 = empty
    val solution: Int,
    val isGiven: Boolean,    // pre-filled, locked
    val isWrong: Boolean,    // value != solution && value != 0
    val isHinted: Boolean,
    val notes: Set<Int>      // pencil candidates 1..9
)
