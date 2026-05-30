package com.lumos.sudoku.core.di

import com.lumos.sudoku.core.datastore.ThemePreferencesRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val coreModule = module {
    single { ThemePreferencesRepository(androidContext()) }
}
