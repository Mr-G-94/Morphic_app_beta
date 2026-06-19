package com.morphiclabs.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.morphiclabs.ui.theme.MorphicLabsAppTheme
import com.morphiclabs.di.AgentRegistry
import com.morphiclabs.agents.KnowledgeAgent
import com.morphiclabs.agents.GatewayAgent
import com.morphiclabs.ui.MorphicLabsScreen
import com.morphiclabs.core.security.AppConfigManager
import com.morphiclabs.ui.screens.SettingsScreen

class MainActivity : ComponentActivity() {
    private val agentRegistry: AgentRegistry by lazy {
        AgentRegistry().apply {
            registerAgent(GatewayAgent(this@MainActivity))
            registerAgent(KnowledgeAgent(this@MainActivity))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MorphicLabsAppTheme {
                MorphicLabsAppEntry(agentRegistry)
            }
        }
    }
}

@Composable
fun MorphicLabsAppEntry(agentRegistry: AgentRegistry) {
    val context = LocalContext.current
    var showSettings by remember { mutableStateOf(false) }
    // Instanciamos el gatewayAgent para pasarlo a SettingsScreen
    val gatewayAgent = remember { GatewayAgent(context) }

    if (showSettings) {
        SettingsScreen(
            configManager = AppConfigManager(context),
            modelProvider = gatewayAgent,
            onBack = { showSettings = false },
            onSave = { showSettings = false }
        )
    } else {
        MorphicLabsScreen(
            onNavigateToSettings = { showSettings = true },
            messageProcessor = object : com.morphiclabs.core.MessageProcessor {
                override suspend fun procesarMensaje(texto: String): String {
                    val agent = agentRegistry.findAgentToHandle(texto)
                    return agent?.execute(texto) ?: "No agent found to handle: "
                }
            }
        )
    }
}
