package com.lumos.sudoku.data.model

sealed class GameState {
    object Idle : GameState()
    object Playing : GameState()
    object Won : GameState()
    object GameOver : GameState()
}
