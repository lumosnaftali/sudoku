package com.lumos.sudoku.feature.game.data.di

import com.lumos.sudoku.feature.game.data.repository.SudokuRepositoryImpl
import com.lumos.sudoku.feature.game.domain.repository.SudokuRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class GameDataModule {

    @Binds
    @Singleton
    abstract fun bindSudokuRepository(impl: SudokuRepositoryImpl): SudokuRepository
}
