package com.lumos.sudoku.core.navigation

object Route {
    const val HOME = "home"
    const val GAME = "game/{difficulty}"
    const val RESULT = "result/{isWon}/{time}/{mistakes}/{hints}/{difficulty}"

    fun game(difficulty: String) = "game/$difficulty"

    fun result(
        isWon: Boolean,
        time: Long,
        mistakes: Int,
        hints: Int,
        difficulty: String
    ) = "result/$isWon/$time/$mistakes/$hints/$difficulty"
}
