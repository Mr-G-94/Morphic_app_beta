package com.morphiclabs.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.morphiclabs.ui.theme.MorphicLabsAppTheme
import com.morphiclabs.di.AgentRegistry
import com.morphiclabs.core.base.AgentContract // Needed for dummy agent if we add one, otherwise not strictly here.
import com.morphiclabs.agents.KnowledgeAgent
import com.morphiclabs.agents.GatewayAgent

// For now, let's include the MorphicLabsScreen from the UI module for initial rendering.
// This will be replaced by a more dynamic shell UI in future phases.
import com.morphiclabs.ui.MorphicLabsScreen

class MainActivity : ComponentActivity() {
    // The AgentRegistry will be injected in a real DI setup.
    // For now, we instantiate it directly in the shell.
    private val agentRegistry: AgentRegistry = AgentRegistry()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Example: Register a dummy agent to show the registry is working
        agentRegistry.registerAgent(object : AgentContract {
            override suspend fun canHandle(command: String): Boolean {
                return command.contains("hello", ignoreCase = true)
            }
            override suspend fun execute(input: String): String {
                return "Hello from Dummy Agent! You said: $input"
            }
        })

        // Register the new KnowledgeAgent
        agentRegistry.registerAgent(KnowledgeAgent(this))

        // Register the new GatewayAgent as the default agent (handles any non-empty command)
        agentRegistry.registerAgent(GatewayAgent(this))

        setContent {
            MorphicLabsAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // The main entry point of the application, now consuming the AgentRegistry
                    MorphicLabsAppEntry(agentRegistry = agentRegistry)
                }
            }
        }
    }
}

@Composable
fun MorphicLabsAppEntry(agentRegistry: AgentRegistry) {
    // This is currently a placeholder for the unified chat UI.
    // In future phases, this will interact with the agentRegistry to dynamically
    // render UI components based on agent responses.
    // For demonstration, we'll keep the existing MorphicLabsScreen but it
    // should ideally be refactored to consume the AgentRegistry itself
    // or be replaced by the actual shell UI.
    MorphicLabsScreen(messageProcessor = object : com.morphiclabs.core.MessageProcessor {
        override suspend fun procesarMensaje(texto: String): String {
            val agent = agentRegistry.findAgentToHandle(texto)
            return agent?.execute(texto) ?: "No agent found to handle: $texto"
        }
    })
}
