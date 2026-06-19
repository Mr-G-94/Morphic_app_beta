package com.morphiclabs.core.dispatchers

import android.content.Context
import com.morphiclabs.di.AgentRegistry
import com.morphiclabs.agents.GatewayAgent
import com.morphiclabs.core.services.InventoryService
import com.morphiclabs.core.services.ClientDB
import com.morphiclabs.core.security.AppConfigManager
import org.json.JSONObject

class BotDispatcher(
    private val agentRegistry: AgentRegistry,
    private val inventoryService: InventoryService,
    private val clientDB: ClientDB,
    private val context: Context
) {
    private val appConfigManager = AppConfigManager(context)

    suspend fun dispatch(source: String, senderId: String, content: String): String {
        val agent = agentRegistry.findAgentToHandle(content)

        val response = if (agent != null) {
            agent.execute(content)
        } else {
            val gateway = agentRegistry.getAgents().filterIsInstance<GatewayAgent>().firstOrNull()
            gateway?.execute(content) ?: "Error: No se pudo procesar tu solicitud."
        }

        return if (response.startsWith("FUNCTION_CALL:")) {
            handleFunctionCall(response)
        } else {
            response
        }
    }

    private suspend fun handleFunctionCall(call: String): String {
        try {
            val parts = call.split(":", limit = 3)
            val functionName = parts[1]
            val args = if (parts.size > 2) JSONObject(parts[2]) else JSONObject()
            
            val activeAgent = appConfigManager.getActiveAgent()

            if (activeAgent != "Ventas") {
                return "Error: El agente actual ('$activeAgent') no tiene permisos para ejecutar funciones de ventas."
            }

            return when (functionName) {
                "obtener_inventario" -> inventoryService.getInventory()
                "calcular_envio" -> {
                    val locationInput = args.optString("ubicacion", "")
                    val address = clientDB.getClientAddress(locationInput) ?: locationInput
                    "Calculando costo de envío a: $address..."
                }
                else -> "Error: Función $functionName no implementada."
            }
        } catch (e: Exception) {
            return "Error al ejecutar la función: ${e.message}"
        }
    }
}
