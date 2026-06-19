package com.morphiclabs.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.morphiclabs.core.MessageProcessor
import com.morphiclabs.ui.screens.DashboardScreen
import com.morphiclabs.ui.screens.ChatScreen
import com.morphiclabs.ui.screens.SettingsScreen
import com.morphiclabs.core.security.AppConfigManager
import com.morphiclabs.core.base.ModelProvider
import androidx.compose.ui.platform.LocalContext

@Composable
fun MainScaffold(
    messageProcessor: MessageProcessor,
    modelProvider: ModelProvider,
    onNavigateToSettings: () -> Unit // Placeholder simple
) {
    var selectedTab by remember { mutableStateOf(0) }
    val context = LocalContext.current
    
    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(selected = selectedTab == 0, onClick = { selectedTab = 0 }, label = { Text("Dashboard") }, icon = { Text("🏠") })
                NavigationBarItem(selected = selectedTab == 1, onClick = { selectedTab = 1 }, label = { Text("Chat") }, icon = { Text("💬") })
                NavigationBarItem(selected = selectedTab == 2, onClick = { selectedTab = 2 }, label = { Text("Settings") }, icon = { Text("⚙️") })
            }
        }
    ) { padding ->
        val contentModifier = Modifier.padding(padding)
        when (selectedTab) {
            0 -> DashboardScreen(onNavigateToSettings = { selectedTab = 2 })
            1 -> ChatScreen(messageProcessor = messageProcessor)
            2 -> SettingsScreen(
                configManager = AppConfigManager(context),
                modelProvider = modelProvider,
                onBack = { selectedTab = 0 },
                onSave = { selectedTab = 0 }
            )
        }
    }
}
