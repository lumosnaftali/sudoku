package com.lumos.sudoku.feature.game.domain.model

sealed class GameState {
    object Idle : GameState()
    object Playing : GameState()
    object Won : GameState()
    object GameOver : GameState()
}
