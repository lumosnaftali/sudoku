package com.lumos.sudoku.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumos.sudoku.data.model.Difficulty
import com.lumos.sudoku.data.repository.ThemePreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val themePreferencesRepository: ThemePreferencesRepository
) : ViewModel() {

    val isDarkTheme: StateFlow<Boolean?> = themePreferencesRepository.isDarkThemeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _selectedDifficulty = MutableStateFlow(Difficulty.EASY)
    val selectedDifficulty: StateFlow<Difficulty> = _selectedDifficulty.asStateFlow()

    fun selectDifficulty(difficulty: Difficulty) {
        _selectedDifficulty.value = difficulty
    }

    fun toggleTheme(isDark: Boolean) {
        viewModelScope.launch {
            themePreferencesRepository.saveThemePreference(isDark)
        }
    }
}
