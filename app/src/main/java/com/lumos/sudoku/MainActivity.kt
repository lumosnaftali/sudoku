package com.lumos.sudoku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.lumos.sudoku.data.repository.ThemePreferencesRepository
import com.lumos.sudoku.ui.navigation.AppNavigation
import com.lumos.sudoku.ui.theme.SudokuTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themePreferencesRepository: ThemePreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            val isDarkThemePref by themePreferencesRepository.isDarkThemeFlow.collectAsState(initial = null)
            val isSystemDark = isSystemInDarkTheme()
            val isDark = isDarkThemePref ?: isSystemDark

            SudokuTheme(darkTheme = isDark) {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    AppNavigation()
                }
            }
        }
    }
}
