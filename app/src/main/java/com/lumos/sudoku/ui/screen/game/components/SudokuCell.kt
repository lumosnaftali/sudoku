package com.lumos.sudoku.ui.screen.game.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumos.sudoku.data.model.SudokuCell
import com.lumos.sudoku.ui.theme.*

@Composable
fun SudokuCellComposable(
    cell: SudokuCell,
    isSelected: Boolean,
    isRelated: Boolean,
    isDarkTheme: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isSelected -> if (isDarkTheme) SelectedCellBgDark else SelectedCellBgLight
        isRelated -> if (isDarkTheme) RelatedCellBgDark else RelatedCellBgLight
        cell.isHinted -> if (isDarkTheme) HintedCellBgDark else HintedCellBgLight
        else -> MaterialTheme.colorScheme.surface
    }

    val textColor = when {
        cell.isGiven -> MaterialTheme.colorScheme.primary
        cell.isWrong -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface
    }

    val fontWeight = if (cell.isGiven) FontWeight.Bold else FontWeight.Normal
    val fontStyle = if (cell.isHinted) FontStyle.Italic else FontStyle.Normal

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (cell.value != 0) {
            Text(
                text = cell.value.toString(),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 20.sp,
                    fontWeight = fontWeight,
                    fontStyle = fontStyle
                ),
                color = textColor
            )
        } else {
            PencilNotesGrid(notes = cell.notes)
        }
    }
}

@Composable
fun PencilNotesGrid(notes: Set<Int>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(1.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        for (r in 0..2) {
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (c in 1..3) {
                    val num = r * 3 + c
                    Box(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (notes.contains(num)) {
                            Text(
                                text = num.toString(),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}
