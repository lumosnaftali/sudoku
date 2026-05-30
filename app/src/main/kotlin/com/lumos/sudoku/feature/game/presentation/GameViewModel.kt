package com.lumos.sudoku.feature.game.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.lumos.sudoku.core.model.Difficulty
import com.lumos.sudoku.core.datastore.ThemePreferencesRepository
import com.lumos.sudoku.feature.game.data.generator.SudokuGenerator
import com.lumos.sudoku.feature.game.data.repository.SudokuRepositoryImpl
import com.lumos.sudoku.feature.game.domain.model.GameState
import com.lumos.sudoku.feature.game.domain.model.SudokuCell
import com.lumos.sudoku.feature.game.domain.usecase.CheckCompletionUseCase
import com.lumos.sudoku.feature.game.domain.usecase.GeneratePuzzleUseCase
import com.lumos.sudoku.feature.game.domain.usecase.GetHintUseCase
import com.lumos.sudoku.feature.game.domain.usecase.ValidateMoveUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Stack

class GameViewModel(
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
            gameState = GameState.Playing,
            difficulty = difficulty,
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

    private fun stopTimer() { timerJob?.cancel() }

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
        val state = _uiState.value
        if (state.gameState != GameState.Playing) return
        val r = state.selectedRow; val c = state.selectedCol
        if (r !in 0..8 || c !in 0..8) return
        val cell = state.board[r][c]
        if (cell.isGiven || cell.isHinted) return
        if (cell.value != 0 && !cell.isWrong) return

        if (state.isPencilMode) {
            val newNotes = if (cell.notes.contains(num)) cell.notes - num else cell.notes + num
            pushToUndoStack(state.board)
            updateCellInBoard(r, c, cell.copy(notes = newNotes, value = 0, isWrong = false))
        } else {
            val conflicts = findConflicts(state.board, r, c, num)
            if (conflicts.isNotEmpty()) { flashConflictCells(conflicts); return }
            pushToUndoStack(state.board)
            val validatedCell = validateMoveUseCase(cell, num)
            updateCellInBoard(r, c, validatedCell)
            if (validatedCell.isWrong) {
                val newMistakes = state.mistakes + 1
                _uiState.update { it.copy(mistakes = newMistakes) }
                if (newMistakes >= 3) { _uiState.update { it.copy(gameState = GameState.GameOver) }; stopTimer() }
            } else {
                if (checkCompletionUseCase(_uiState.value.board)) {
                    _uiState.update { it.copy(gameState = GameState.Won) }; stopTimer()
                }
            }
        }
    }

    fun eraseSelected() {
        val state = _uiState.value
        if (state.gameState != GameState.Playing) return
        val r = state.selectedRow; val c = state.selectedCol
        if (r !in 0..8 || c !in 0..8) return
        val cell = state.board[r][c]
        if (cell.isGiven || cell.isHinted) return
        if (cell.value != 0 && !cell.isWrong) return
        if (cell.value == 0 && cell.notes.isEmpty()) return
        pushToUndoStack(state.board)
        updateCellInBoard(r, c, cell.copy(value = 0, isWrong = false, notes = emptySet()))
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
        if (_uiState.value.numberRemainingCounts.getOrElse(num - 1) { 0 } <= 0) return
        _uiState.update { it.copy(selectedNumber = num) }
    }

    fun useHint() {
        val state = _uiState.value
        if (state.gameState != GameState.Playing) return
        val r = state.selectedRow; val c = state.selectedCol
        if (r !in 0..8 || c !in 0..8) return
        val cell = state.board[r][c]
        if (cell.isGiven || cell.value == cell.solution) return
        pushToUndoStack(state.board)
        updateCellInBoard(r, c, getHintUseCase(cell))
        _uiState.update { it.copy(hintsUsed = it.hintsUsed + 1) }
        if (checkCompletionUseCase(_uiState.value.board)) {
            _uiState.update { it.copy(gameState = GameState.Won) }; stopTimer()
        }
    }

    fun undo() {
        if (_uiState.value.gameState != GameState.Playing) return
        if (undoStack.isNotEmpty()) {
            val previousBoard = undoStack.pop()
            val state = _uiState.value
            val selectedNumber = if (state.selectedRow in 0..8 && state.selectedCol in 0..8)
                previousBoard[state.selectedRow][state.selectedCol].value else 0
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
        viewModelScope.launch { themePreferencesRepository.saveThemePreference(isDark) }
    }

    private fun findConflicts(board: List<List<SudokuCell>>, row: Int, col: Int, num: Int): Set<Pair<Int, Int>> {
        val conflicts = mutableSetOf<Pair<Int, Int>>()
        for (c in 0..8) if (c != col && board[row][c].value == num) conflicts.add(row to c)
        for (r in 0..8) if (r != row && board[r][col].value == num) conflicts.add(r to col)
        val boxRow = (row / 3) * 3; val boxCol = (col / 3) * 3
        for (r in boxRow until boxRow + 3) for (c in boxCol until boxCol + 3)
            if ((r != row || c != col) && board[r][c].value == num) conflicts.add(r to c)
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
        undoStack.push(board.map { row -> row.map { it.copy() } })
    }

    private fun updateCellInBoard(row: Int, col: Int, newCell: SudokuCell) {
        _uiState.update { state ->
            val newBoard = state.board.mapIndexed { rIndex, rList ->
                if (rIndex == row) rList.mapIndexed { cIndex, cell -> if (cIndex == col) newCell else cell }
                else rList
            }
            state.copy(board = newBoard, numberRemainingCounts = computeRemainingCounts(newBoard))
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
        conflictFlashJob?.cancel()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                val repository = SudokuRepositoryImpl(SudokuGenerator())
                GameViewModel(
                    generatePuzzleUseCase = GeneratePuzzleUseCase(repository),
                    validateMoveUseCase = ValidateMoveUseCase(),
                    getHintUseCase = GetHintUseCase(),
                    checkCompletionUseCase = CheckCompletionUseCase(),
                    themePreferencesRepository = ThemePreferencesRepository(app)
                )
            }
        }
    }
}
