package com.morphiclabs.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.morphiclabs.ui.components.MorphicCard
import com.morphiclabs.ui.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(onNavigateToSettings: () -> Unit, viewModel: DashboardViewModel = viewModel()) {
    val stats by viewModel.stats.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToSettings, containerColor = MaterialTheme.colorScheme.primary) {
                Text("⚙️")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text("Morphic Control", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))

            // Stats Cards
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MorphicCard(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("CPU", color = MaterialTheme.colorScheme.onSurface)
                        Text("${(stats.cpuUsage * 100).toInt()}%", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
                MorphicCard(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("RAM", color = MaterialTheme.colorScheme.onSurface)
                        Text("${(stats.memUsage * 100).toInt()}%", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // Logs
            Text("Live Logs", color = MaterialTheme.colorScheme.primary)
            MorphicCard(modifier = Modifier.height(200.dp)) {
                LazyColumn(modifier = Modifier.padding(8.dp)) {
                    items(stats.logs) { log ->
                        Text(" > $log", color = MaterialTheme.colorScheme.onSurface, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}
