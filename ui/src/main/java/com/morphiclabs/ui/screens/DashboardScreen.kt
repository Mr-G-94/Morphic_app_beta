package com.morphiclabs.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.morphiclabs.ui.components.MorphicCard
import com.morphiclabs.ui.components.MorphicProgressBar
import com.morphiclabs.ui.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(onNavigateToSettings: () -> Unit, viewModel: DashboardViewModel = viewModel()) {
    val stats by viewModel.stats.collectAsState()
    var performanceBoost by remember { mutableFloatStateOf(80f) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("COMMAND CENTER", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))

        // Stats Cards (CPU/RAM)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MorphicCard(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("CPU", color = MaterialTheme.colorScheme.onSurface)
                    MorphicProgressBar(progress = stats.cpuUsage)
                }
            }
            MorphicCard(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("RAM", color = MaterialTheme.colorScheme.onSurface)
                    MorphicProgressBar(progress = stats.memUsage)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Security Status
        MorphicCard {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Security Status", color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(8.dp))
                // Visualizador de arco simple
                Canvas(modifier = Modifier.size(100.dp, 60.dp)) {
                    drawArc(color = Color(0xFFC6FF00), startAngle = 180f, sweepAngle = 180f, useCenter = false, style = Stroke(width = 15f, cap = StrokeCap.Round))
                }
                Text("LOW THREAT", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleLarge)
                Button(onClick = {}, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                    Text("SCAN SYSTEM", color = Color.Black)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live Logs
        Text("Live Logs", color = MaterialTheme.colorScheme.primary)
        MorphicCard(modifier = Modifier.height(150.dp)) {
            LazyColumn(modifier = Modifier.padding(8.dp)) {
                items(stats.logs) { log ->
                    Text(" > $log", color = MaterialTheme.colorScheme.onSurface, fontFamily = FontFamily.Monospace)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tools
        MorphicCard {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("VPN Connection")
                    Switch(checked = true, onCheckedChange = {})
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("AdBlocker")
                    Switch(checked = false, onCheckedChange = {})
                }
                Text("Performance Boost: ${performanceBoost.toInt()}%")
                Slider(value = performanceBoost, onValueChange = { performanceBoost = it }, valueRange = 0f..100f)
            }
        }
    }
}
