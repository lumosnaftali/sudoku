package com.lumos.sudoku.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.lumos.sudoku.SudokuApplication
import com.lumos.sudoku.core.model.Difficulty
import com.lumos.sudoku.core.datastore.ThemePreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val themePreferencesRepository: ThemePreferencesRepository
) : ViewModel() {

    private val _selectedDifficulty = MutableStateFlow(Difficulty.EASY)

    val uiState: StateFlow<HomeUiState> = combine(
        _selectedDifficulty,
        themePreferencesRepository.isDarkThemeFlow
    ) { difficulty, isDarkTheme ->
        HomeUiState(selectedDifficulty = difficulty, isDarkTheme = isDarkTheme)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun selectDifficulty(difficulty: Difficulty) {
        _selectedDifficulty.value = difficulty
    }

    fun toggleTheme(isDark: Boolean) {
        viewModelScope.launch {
            themePreferencesRepository.saveThemePreference(isDark)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) as SudokuApplication
                HomeViewModel(app.themeRepository)
            }
        }
    }
}
