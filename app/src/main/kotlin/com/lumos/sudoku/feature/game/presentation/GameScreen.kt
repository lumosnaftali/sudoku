package com.lumos.sudoku.feature.game.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumos.sudoku.feature.game.domain.model.GameState
import com.lumos.sudoku.feature.game.presentation.component.GameControls
import com.lumos.sudoku.feature.game.presentation.component.NumberPad
import com.lumos.sudoku.feature.game.presentation.component.SudokuGrid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onBackToHome: () -> Unit,
    onGameFinished: (Boolean, Long, Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isDarkThemePref by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val isDark = isDarkThemePref ?: isSystemInDarkTheme()

    LaunchedEffect(uiState.gameState) {
        when (uiState.gameState) {
            GameState.Won -> onGameFinished(true, uiState.elapsedSeconds, uiState.mistakes, uiState.hintsUsed)
            GameState.GameOver -> onGameFinished(false, uiState.elapsedSeconds, uiState.mistakes, uiState.hintsUsed)
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.difficulty.label.uppercase(),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 18.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackToHome) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to home")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleTheme(!isDark) }) {
                        Icon(
                            imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mistakes: ${uiState.mistakes}/3",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.mistakes > 0) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timer, contentDescription = "Timer",
                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = formatTime(uiState.elapsedSeconds),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    )
                }
                Text(
                    text = "Hints: ${uiState.hintsUsed}",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                )
            }

            SudokuGrid(
                board = uiState.board,
                selectedRow = uiState.selectedRow,
                selectedCol = uiState.selectedCol,
                selectedNumber = uiState.selectedNumber,
                conflictCells = uiState.conflictCells,
                isDarkTheme = isDark,
                onCellClick = { r, c -> viewModel.selectCell(r, c) },
                modifier = Modifier.weight(1f, fill = false)
            )

            Spacer(Modifier.height(16.dp))

            GameControls(
                isPencilMode = uiState.isPencilMode,
                isInstantFillMode = uiState.isInstantFillMode,
                onUndoClick = { viewModel.undo() },
                onPencilToggle = { viewModel.togglePencilMode() },
                onHintClick = { viewModel.useHint() },
                onInstantFillToggle = { viewModel.toggleInstantFillMode() }
            )

            Spacer(Modifier.height(16.dp))

            NumberPad(
                onNumberClick = { num ->
                    if (uiState.isInstantFillMode) viewModel.setSelectedNumber(num)
                    else viewModel.enterNumber(num)
                },
                onEraseClick = { viewModel.eraseSelected() },
                selectedNumber = uiState.selectedNumber,
                numberRemainingCounts = uiState.numberRemainingCounts
            )

            Spacer(Modifier.height(8.dp))
        }
    }
}

private fun formatTime(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", mins, secs)
}
