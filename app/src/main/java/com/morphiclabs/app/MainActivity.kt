package com.morphiclabs.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.morphiclabs.ui.theme.MorphicLabsAppTheme
import com.morphiclabs.di.AgentRegistry
import com.morphiclabs.core.base.AgentContract
import com.morphiclabs.agents.KnowledgeAgent
import com.morphiclabs.agents.GatewayAgent
import com.morphiclabs.ui.MorphicLabsScreen
import com.morphiclabs.core.security.AppConfigManager
import com.morphiclabs.ui.screens.SettingsScreen

class MainActivity : ComponentActivity() {
    private val agentRegistry: AgentRegistry = AgentRegistry()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        agentRegistry.registerAgent(object : AgentContract {
            override suspend fun canHandle(command: String): Boolean {
                return command.contains("hello", ignoreCase = true)
            }
            override suspend fun execute(input: String): String {
                return "Hello from Dummy Agent! You said: $input"
            }
        })

        agentRegistry.registerAgent(KnowledgeAgent(this))
        agentRegistry.registerAgent(GatewayAgent(this))

        setContent {
            MorphicLabsAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val configManager = remember { AppConfigManager(this) }
                    var showSettings by remember { mutableStateOf(false) }

                    if (showSettings) {
                        SettingsScreen(configManager, onBack = { showSettings = false })
                    } else {
                        Box(modifier = Modifier.fillMaxSize()) {
                            MorphicLabsAppEntry(agentRegistry = agentRegistry)
                            
                            // Botón flotante para Settings
                            FloatingActionButton(
                                onClick = { showSettings = true },
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(16.dp)
                            ) {
                                Text("⚙️")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MorphicLabsAppEntry(agentRegistry: AgentRegistry) {
    MorphicLabsScreen(messageProcessor = object : com.morphiclabs.core.MessageProcessor {
        override suspend fun procesarMensaje(texto: String): String {
            val agent = agentRegistry.findAgentToHandle(texto)
            return agent?.execute(texto) ?: "No agent found to handle: $texto"
        }
    })
}
