package com.lumos.sudoku

import android.app.Application
import com.lumos.sudoku.core.di.coreModule
import com.lumos.sudoku.feature.game.di.gameModule
import com.lumos.sudoku.feature.home.di.homeModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class SudokuApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@SudokuApplication)
            modules(coreModule, gameModule, homeModule)
        }
    }
}
