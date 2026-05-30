package com.lumos.sudoku.ui.screen.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumos.sudoku.data.model.Difficulty
import com.lumos.sudoku.data.model.GameState
import com.lumos.sudoku.data.model.SudokuCell
import com.lumos.sudoku.data.repository.ThemePreferencesRepository
import com.lumos.sudoku.domain.usecase.CheckCompletionUseCase
import com.lumos.sudoku.domain.usecase.GeneratePuzzleUseCase
import com.lumos.sudoku.domain.usecase.GetHintUseCase
import com.lumos.sudoku.domain.usecase.ValidateMoveUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Stack
import javax.inject.Inject

data class GameUiState(
    val board: List<List<SudokuCell>> = emptyList(),
    val selectedRow: Int = -1,
    val selectedCol: Int = -1,
    val isPencilMode: Boolean = false,
    val mistakes: Int = 0,
    val hintsUsed: Int = 0,
    val gameState: GameState = GameState.Idle,
    val difficulty: Difficulty = Difficulty.EASY,
    val elapsedSeconds: Long = 0
)

@HiltViewModel
class GameViewModel @Inject constructor(
    private val generatePuzzleUseCase: GeneratePuzzleUseCase,
    private val validateMoveUseCase: ValidateMoveUseCase,
    private val getHintUseCase: GetHintUseCase,
    private val checkCompletionUseCase: CheckCompletionUseCase,
    private val themePreferencesRepository: ThemePreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    val isDarkTheme: StateFlow<Boolean?> = themePreferencesRepository.isDarkThemeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val undoStack = Stack<List<List<SudokuCell>>>()
    private var timerJob: Job? = null

    fun initGame(difficulty: Difficulty) {
        val newBoard = generatePuzzleUseCase(difficulty)
        undoStack.clear()
        
        _uiState.value = GameUiState(
            board = newBoard,
            selectedRow = -1,
            selectedCol = -1,
            isPencilMode = false,
            mistakes = 0,
            hintsUsed = 0,
            gameState = GameState.Playing,
            difficulty = difficulty,
            elapsedSeconds = 0
        )
        
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                _uiState.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
    }

    fun selectCell(row: Int, col: Int) {
        if (_uiState.value.gameState != GameState.Playing) return
        _uiState.update {
            it.copy(selectedRow = row, selectedCol = col)
        }
    }

    fun enterNumber(num: Int) {
        val currentState = _uiState.value
        if (currentState.gameState != GameState.Playing) return
        
        val r = currentState.selectedRow
        val c = currentState.selectedCol
        if (r !in 0..8 || c !in 0..8) return

        val cell = currentState.board[r][c]
        if (cell.isGiven || cell.isHinted) return
        if (cell.value == num && cell.notes.isEmpty()) return

        // Push copy to undo stack
        pushToUndoStack(currentState.board)

        if (currentState.isPencilMode) {
            // Pencil/Notes Mode: toggle note
            val newNotes = if (cell.notes.contains(num)) {
                cell.notes - num
            } else {
                cell.notes + num
            }
            val updatedCell = cell.copy(notes = newNotes, value = 0, isWrong = false)
            updateCellInBoard(r, c, updatedCell)
        } else {
            // Normal Mode: enter confirmed value
            val validatedCell = validateMoveUseCase(cell, num)
            updateCellInBoard(r, c, validatedCell)

            if (validatedCell.isWrong) {
                val newMistakes = currentState.mistakes + 1
                _uiState.update { it.copy(mistakes = newMistakes) }
                if (newMistakes >= 3) {
                    _uiState.update { it.copy(gameState = GameState.GameOver) }
                    stopTimer()
                }
            } else {
                // Check if puzzle is complete and correct
                val isComplete = checkCompletionUseCase(_uiState.value.board)
                if (isComplete) {
                    _uiState.update { it.copy(gameState = GameState.Won) }
                    stopTimer()
                }
            }
        }
    }

    fun eraseSelected() {
        val currentState = _uiState.value
        if (currentState.gameState != GameState.Playing) return

        val r = currentState.selectedRow
        val c = currentState.selectedCol
        if (r !in 0..8 || c !in 0..8) return

        val cell = currentState.board[r][c]
        if (cell.isGiven || cell.isHinted) return
        if (cell.value == 0 && cell.notes.isEmpty()) return

        pushToUndoStack(currentState.board)

        val updatedCell = cell.copy(value = 0, isWrong = false, notes = emptySet())
        updateCellInBoard(r, c, updatedCell)
    }

    fun togglePencilMode() {
        if (_uiState.value.gameState != GameState.Playing) return
        _uiState.update { it.copy(isPencilMode = !it.isPencilMode) }
    }

    fun useHint() {
        val currentState = _uiState.value
        if (currentState.gameState != GameState.Playing) return

        val r = currentState.selectedRow
        val c = currentState.selectedCol
        if (r !in 0..8 || c !in 0..8) return

        val cell = currentState.board[r][c]
        if (cell.isGiven || cell.value == cell.solution) return

        pushToUndoStack(currentState.board)

        val hintedCell = getHintUseCase(cell)
        updateCellInBoard(r, c, hintedCell)

        _uiState.update { it.copy(hintsUsed = it.hintsUsed + 1) }

        val isComplete = checkCompletionUseCase(_uiState.value.board)
        if (isComplete) {
            _uiState.update { it.copy(gameState = GameState.Won) }
            stopTimer()
        }
    }

    fun undo() {
        if (_uiState.value.gameState != GameState.Playing) return
        if (undoStack.isNotEmpty()) {
            val previousBoard = undoStack.pop()
            _uiState.update { it.copy(board = previousBoard) }
        }
    }

    fun toggleTheme(isDark: Boolean) {
        viewModelScope.launch {
            themePreferencesRepository.saveThemePreference(isDark)
        }
    }

    private fun pushToUndoStack(board: List<List<SudokuCell>>) {
        val boardCopy = board.map { row -> row.map { it.copy() } }
        undoStack.push(boardCopy)
    }

    private fun updateCellInBoard(row: Int, col: Int, newCell: SudokuCell) {
        _uiState.update { state ->
            val newBoard = state.board.mapIndexed { rIndex, rList ->
                if (rIndex == row) {
                    rList.mapIndexed { cIndex, cell ->
                        if (cIndex == col) newCell else cell
                    }
                } else {
                    rList
                }
            }
            state.copy(board = newBoard)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}
