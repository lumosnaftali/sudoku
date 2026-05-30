package com.lumos.sudoku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumos.sudoku.core.datastore.ThemePreferencesRepository
import com.lumos.sudoku.core.navigation.AppNavigation
import com.lumos.sudoku.core.theme.SudokuTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val themeRepository = ThemePreferencesRepository(applicationContext)

        setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides this@MainActivity) {
                val isDarkThemePref by themeRepository.isDarkThemeFlow
                    .collectAsStateWithLifecycle(initialValue = null)
                val isDark = isDarkThemePref ?: isSystemInDarkTheme()

                SudokuTheme(darkTheme = isDark) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        AppNavigation()
                    }
                }
            }
        }
    }
}
