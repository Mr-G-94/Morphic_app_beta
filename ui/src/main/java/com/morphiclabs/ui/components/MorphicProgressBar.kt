package com.morphiclabs.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp

@Composable
fun MorphicProgressBar(progress: Float, modifier: Modifier = Modifier) {
    val barColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)

    Canvas(modifier = modifier.fillMaxWidth().height(8.dp)) {
        // Fondo de la barra
        drawRoundRect(
            color = backgroundColor,
            size = Size(size.width, size.height),
            cornerRadius = CornerRadius(4.dp.toPx())
        )
        // Progreso
        drawRoundRect(
            color = barColor,
            size = Size(size.width * progress.coerceIn(0f, 1f), size.height),
            cornerRadius = CornerRadius(4.dp.toPx())
        )
    }
}
