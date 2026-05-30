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
    val elapsedSeconds: Long = 0,
    val selectedNumber: Int = 0,
    val isInstantFillMode: Boolean = false,
    val conflictCells: Set<Pair<Int, Int>> = emptySet(),
    val numberRemainingCounts: List<Int> = List(9) { 9 }
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
    private var conflictFlashJob: Job? = null

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
            elapsedSeconds = 0,
            selectedNumber = 0,
            isInstantFillMode = false,
            conflictCells = emptySet(),
            numberRemainingCounts = computeRemainingCounts(newBoard)
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
        val state = _uiState.value
        if (state.isInstantFillMode && state.selectedNumber != 0) {
            _uiState.update { it.copy(selectedRow = row, selectedCol = col) }
            enterNumber(state.selectedNumber)
        } else {
            val cellValue = state.board[row][col].value
            _uiState.update { it.copy(selectedRow = row, selectedCol = col, selectedNumber = cellValue) }
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
        if (cell.value != 0 && !cell.isWrong) return  // Feature 1: lock correctly filled cells

        if (currentState.isPencilMode) {
            val newNotes = if (cell.notes.contains(num)) cell.notes - num else cell.notes + num
            val updatedCell = cell.copy(notes = newNotes, value = 0, isWrong = false)
            pushToUndoStack(currentState.board)
            updateCellInBoard(r, c, updatedCell)
        } else {
            // Feature 3: block Sudoku constraint violations
            val conflicts = findConflicts(currentState.board, r, c, num)
            if (conflicts.isNotEmpty()) {
                flashConflictCells(conflicts)
                return
            }

            pushToUndoStack(currentState.board)
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
        if (cell.value != 0 && !cell.isWrong) return  // Feature 1: lock correctly filled cells
        if (cell.value == 0 && cell.notes.isEmpty()) return

        pushToUndoStack(currentState.board)
        val updatedCell = cell.copy(value = 0, isWrong = false, notes = emptySet())
        updateCellInBoard(r, c, updatedCell)
    }

    fun togglePencilMode() {
        if (_uiState.value.gameState != GameState.Playing) return
        val turningOn = !_uiState.value.isPencilMode
        _uiState.update {
            it.copy(
                isPencilMode = turningOn,
                isInstantFillMode = if (turningOn) false else it.isInstantFillMode,
                selectedNumber = if (turningOn) 0 else it.selectedNumber
            )
        }
    }

    fun toggleInstantFillMode() {
        if (_uiState.value.gameState != GameState.Playing) return
        val turningOn = !_uiState.value.isInstantFillMode
        _uiState.update {
            it.copy(
                isInstantFillMode = turningOn,
                isPencilMode = if (turningOn) false else it.isPencilMode,
                selectedNumber = if (!turningOn) 0 else it.selectedNumber
            )
        }
    }

    fun setSelectedNumber(num: Int) {
        if (_uiState.value.gameState != GameState.Playing) return
        val remaining = _uiState.value.numberRemainingCounts.getOrElse(num - 1) { 0 }
        if (remaining <= 0) return
        _uiState.update { it.copy(selectedNumber = num) }
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
            val state = _uiState.value
            val selectedNumber = if (state.selectedRow in 0..8 && state.selectedCol in 0..8) {
                previousBoard[state.selectedRow][state.selectedCol].value
            } else 0
            _uiState.update {
                it.copy(
                    board = previousBoard,
                    numberRemainingCounts = computeRemainingCounts(previousBoard),
                    selectedNumber = selectedNumber
                )
            }
        }
    }

    fun toggleTheme(isDark: Boolean) {
        viewModelScope.launch {
            themePreferencesRepository.saveThemePreference(isDark)
        }
    }

    private fun findConflicts(
        board: List<List<SudokuCell>>,
        row: Int,
        col: Int,
        num: Int
    ): Set<Pair<Int, Int>> {
        val conflicts = mutableSetOf<Pair<Int, Int>>()
        for (c in 0..8) {
            if (c != col && board[row][c].value == num) conflicts.add(row to c)
        }
        for (r in 0..8) {
            if (r != row && board[r][col].value == num) conflicts.add(r to col)
        }
        val boxRow = (row / 3) * 3
        val boxCol = (col / 3) * 3
        for (r in boxRow until boxRow + 3) {
            for (c in boxCol until boxCol + 3) {
                if ((r != row || c != col) && board[r][c].value == num) conflicts.add(r to c)
            }
        }
        return conflicts
    }

    private fun flashConflictCells(conflicts: Set<Pair<Int, Int>>) {
        conflictFlashJob?.cancel()
        _uiState.update { it.copy(conflictCells = conflicts) }
        conflictFlashJob = viewModelScope.launch {
            delay(600)
            _uiState.update { it.copy(conflictCells = emptySet()) }
        }
    }

    private fun computeRemainingCounts(board: List<List<SudokuCell>>): List<Int> {
        val placed = IntArray(9)
        for (row in board) for (cell in row) if (cell.value in 1..9) placed[cell.value - 1]++
        return List(9) { i -> 9 - placed[i] }
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
            state.copy(board = newBoard, numberRemainingCounts = computeRemainingCounts(newBoard))
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
        conflictFlashJob?.cancel()
    }
}
