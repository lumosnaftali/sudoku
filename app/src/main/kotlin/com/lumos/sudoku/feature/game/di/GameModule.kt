package com.lumos.sudoku.feature.game.di

import com.lumos.sudoku.feature.game.data.generator.SudokuGenerator
import com.lumos.sudoku.feature.game.data.repository.SudokuRepositoryImpl
import com.lumos.sudoku.feature.game.domain.repository.SudokuRepository
import com.lumos.sudoku.feature.game.domain.usecase.CheckCompletionUseCase
import com.lumos.sudoku.feature.game.domain.usecase.GeneratePuzzleUseCase
import com.lumos.sudoku.feature.game.domain.usecase.GetHintUseCase
import com.lumos.sudoku.feature.game.domain.usecase.ValidateMoveUseCase
import com.lumos.sudoku.feature.game.presentation.GameViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val gameModule = module {
    single { SudokuGenerator() }
    single<SudokuRepository> { SudokuRepositoryImpl(get()) }

    factory { GeneratePuzzleUseCase(get()) }
    factory { ValidateMoveUseCase() }
    factory { GetHintUseCase() }
    factory { CheckCompletionUseCase() }

    viewModel { GameViewModel(get(), get(), get(), get(), get()) }
}
