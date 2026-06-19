package com.morphiclabs.app

import android.content.Intent
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
import com.morphiclabs.app.services.BotService

class MainActivity : ComponentActivity() {
    // Inicialización con registro de agentes
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
    MorphicLabsScreen(messageProcessor = object : com.morphiclabs.core.MessageProcessor {
        override suspend fun procesarMensaje(texto: String): String {
            val agent = agentRegistry.findAgentToHandle(texto)
            return agent?.execute(texto) ?: "No agent found to handle: $texto"
        }
    })
}
