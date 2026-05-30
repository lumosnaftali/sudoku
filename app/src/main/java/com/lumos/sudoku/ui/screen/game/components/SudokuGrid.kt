package com.lumos.sudoku.ui.screen.game.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.lumos.sudoku.data.model.SudokuCell

@Composable
fun SudokuGrid(
    board: List<List<SudokuCell>>,
    selectedRow: Int,
    selectedCol: Int,
    isDarkTheme: Boolean,
    onCellClick: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val dividerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    val thickDividerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
    val outerBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .border(3.dp, outerBorderColor, RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        if (board.isEmpty() || board.size < 9 || board.any { it.size < 9 }) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                for (r in 0..8) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        for (c in 0..8) {
                            val cell = board[r][c]
                            val isSelected = r == selectedRow && c == selectedCol
                            val isRelated = !isSelected && selectedRow != -1 && selectedCol != -1 && (
                                r == selectedRow ||
                                c == selectedCol ||
                                (r / 3 == selectedRow / 3 && c / 3 == selectedCol / 3)
                            )

                            SudokuCellComposable(
                                cell = cell,
                                isSelected = isSelected,
                                isRelated = isRelated,
                                isDarkTheme = isDarkTheme,
                                onClick = { onCellClick(r, c) },
                                modifier = Modifier.weight(1f)
                            )

                            if (c < 8) {
                                val thickness = if (c == 2 || c == 5) 2.dp else 0.5.dp
                                val color = if (c == 2 || c == 5) thickDividerColor else dividerColor
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .width(thickness)
                                        .background(color)
                                )
                            }
                        }
                    }

                    if (r < 8) {
                        val thickness = if (r == 2 || r == 5) 2.dp else 0.5.dp
                        val color = if (r == 2 || r == 5) thickDividerColor else dividerColor
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(thickness)
                                .background(color)
                        )
                    }
                }
            }
        }
    }
}
