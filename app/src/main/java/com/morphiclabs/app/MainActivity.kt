package com.morphiclabs.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.morphiclabs.ui.theme.MorphicLabsAppTheme
import com.morphiclabs.di.AgentRegistry
import com.morphiclabs.agents.KnowledgeAgent
import com.morphiclabs.agents.GatewayAgent
import com.morphiclabs.ui.MainScaffold

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
                val gatewayAgent = GatewayAgent(this)
                MainScaffold(
                    messageProcessor = object : com.morphiclabs.core.MessageProcessor {
                        override suspend fun procesarMensaje(texto: String): String {
                            val agent = agentRegistry.findAgentToHandle(texto)
                            return agent?.execute(texto) ?: "No agent found"
                        }
                    },
                    modelProvider = gatewayAgent,
                    onNavigateToSettings = {} 
                )
            }
        }
    }
}
