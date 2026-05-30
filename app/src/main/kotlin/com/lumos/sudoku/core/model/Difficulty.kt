package com.lumos.sudoku.core.model

enum class Difficulty(val label: String, val revealedCellsRange: IntRange) {
    EASY("Easy", 45..50),
    MEDIUM("Medium", 35..44),
    HARD("Hard", 25..34)
}
