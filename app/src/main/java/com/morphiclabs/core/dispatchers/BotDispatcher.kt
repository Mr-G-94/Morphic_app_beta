package com.morphiclabs.core.dispatchers

import com.morphiclabs.di.AgentRegistry
import com.morphiclabs.agents.GatewayAgent

class BotDispatcher(private val agentRegistry: AgentRegistry) {

    // Función central para procesar cualquier mensaje
    suspend fun dispatch(source: String, senderId: String, content: String): String {
        // Aquí podrías añadir lógica para logs, por ejemplo:
        // println("Mensaje recibido desde $source del usuario $senderId: $content")

        // 1. Intentamos buscar un agente específico (ej: conocimiento, ventas)
        val agent = agentRegistry.findAgentToHandle(content)

        // 2. Si no hay un agente específico, delegamos al Gateway (IA)
        val response = if (agent != null) {
            agent.execute(content)
        } else {
            // Buscamos el Gateway registrado en el registry
            val gateway = agentRegistry.getAgents().filterIsInstance<GatewayAgent>().firstOrNull()
            gateway?.execute(content) ?: "Error: No se pudo procesar tu solicitud."
        }

        return response
    }
}
