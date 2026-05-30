package com.lumos.sudoku.ui.screen.game.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GameControls(
    isPencilMode: Boolean,
    isInstantFillMode: Boolean,
    onUndoClick: () -> Unit,
    onPencilToggle: () -> Unit,
    onHintClick: () -> Unit,
    onInstantFillToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ControlItem(
            icon = Icons.AutoMirrored.Filled.Undo,
            label = "Undo",
            onClick = onUndoClick
        )

        ControlItem(
            icon = Icons.Default.Create,
            label = "Notes",
            isActive = isPencilMode,
            onClick = onPencilToggle
        )

        ControlItem(
            icon = Icons.Default.FlashOn,
            label = "Fill",
            isActive = isInstantFillMode,
            onClick = onInstantFillToggle
        )

        ControlItem(
            icon = Icons.Default.Lightbulb,
            label = "Hint",
            onClick = onHintClick
        )
    }
}

@Composable
fun ControlItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    isActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isActive) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    }

    val contentColor = if (isActive) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor)
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 20.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            ),
            color = contentColor
        )
    }
}
