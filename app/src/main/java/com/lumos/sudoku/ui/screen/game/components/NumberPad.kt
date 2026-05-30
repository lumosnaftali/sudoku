package com.lumos.sudoku.ui.screen.game.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NumberPad(
    onNumberClick: (Int) -> Unit,
    onEraseClick: () -> Unit,
    selectedNumber: Int = 0,
    numberRemainingCounts: List<Int> = List(9) { 9 },
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (num in 1..5) {
                val remaining = numberRemainingCounts.getOrElse(num - 1) { 0 }
                NumberButton(
                    number = num,
                    remaining = remaining,
                    isActive = num == selectedNumber,
                    onClick = { if (remaining > 0) onNumberClick(num) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (num in 6..9) {
                val remaining = numberRemainingCounts.getOrElse(num - 1) { 0 }
                NumberButton(
                    number = num,
                    remaining = remaining,
                    isActive = num == selectedNumber,
                    onClick = { if (remaining > 0) onNumberClick(num) },
                    modifier = Modifier.weight(1f)
                )
            }

            EraseButton(
                onClick = onEraseClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun NumberButton(
    number: Int,
    remaining: Int,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isExhausted = remaining <= 0
    val bgColor = if (isExhausted) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    }
    val textColor = if (isExhausted) {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
    } else {
        MaterialTheme.colorScheme.primary
    }

    val borderModifier = if (isActive && !isExhausted) {
        Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .alpha(if (isExhausted) 0.35f else 1f)
            .then(borderModifier)
            .aspectRatio(1.2f)
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .clickable(enabled = !isExhausted) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                ),
                color = textColor
            )
            Text(
                text = remaining.toString(),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
            )
        }
    }
}

@Composable
fun EraseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1.2f)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Erase",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(26.dp)
        )
    }
}
