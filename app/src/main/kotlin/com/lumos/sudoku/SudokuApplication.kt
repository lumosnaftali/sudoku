package com.lumos.sudoku

import android.app.Application
import com.lumos.sudoku.core.datastore.ThemePreferencesRepository

class SudokuApplication : Application() {

    // Shared singleton — DataStore requires one instance per file per process
    val themeRepository: ThemePreferencesRepository by lazy {
        ThemePreferencesRepository(this)
    }
}
