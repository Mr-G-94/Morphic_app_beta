package com.morphiclabs.agents

import android.content.Context
import com.morphiclabs.core.base.AgentContract
import com.morphiclabs.core.base.ModelProvider
import com.morphiclabs.core.security.AppConfigManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class GatewayAgent(private val context: Context) : AgentContract, ModelProvider {
    private val client = OkHttpClient()
    private val appConfigManager = AppConfigManager(context)

    override suspend fun fetchAvailableModels(apiKey: String): List<String> = withContext(Dispatchers.IO) {
        val url = "https://generativelanguage.googleapis.com/v1beta/models?key=$apiKey"
        val request = Request.Builder().url(url).get().build()
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val responseData = response.body?.string() ?: return@withContext emptyList()
                val json = JSONObject(responseData)
                val modelsArray = json.getJSONArray("models")
                val list = mutableListOf<String>()
                for (i in 0 until modelsArray.length()) {
                    val modelName = modelsArray.getJSONObject(i).getString("name")
                    if (modelName.startsWith("models/gemini")) {
                        list.add(modelName.replace("models/", ""))
                    }
                }
                return@withContext list
            }
        } catch (e: Exception) { return@withContext emptyList() }
    }

    override suspend fun canHandle(command: String): Boolean = command.isNotEmpty()

    override suspend fun execute(input: String): String = withContext(Dispatchers.IO) {
        val apiKey = appConfigManager.getApiKey("gemini")
        if (apiKey.isNullOrEmpty()) return@withContext "Error: API Key no configurada."

        val modelName = appConfigManager.getModel().trim()
        val activeAgent = appConfigManager.getActiveAgent()

        val systemInstruction = when(activeAgent) {
            "Ventas" -> "Eres la achichinclera de Emilio. Gestiona inventario y envíos usando las herramientas disponibles."
            "Código" -> "Eres el ayudante de Mr. G. Técnico, preciso y experto en arquitectura Android."
            else -> "Eres un asistente general de Morphic Labs, servicial y eficiente."
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"

        val jsonBody = JSONObject().apply {
            put("system_instruction", JSONObject().apply {
                put("parts", JSONArray().apply { put(JSONObject().apply { put("text", systemInstruction) }) })
            })
            // Aquí inyectamos las herramientas filtradas por el agente activo
            put("tools", getToolsDefinition(activeAgent)) 
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply { put(JSONObject().apply { put("text", input) }) })
                })
            })
        }
        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder().url(url).post(requestBody).build()

        try {
            client.newCall(request).execute().use { response ->
                val responseData = response.body?.string()
                if (!response.isSuccessful) return@withContext "Error HTTP ${response.code}: $responseData"
                return@withContext if (responseData != null) parseResponse(responseData) else "Error: Respuesta vacía."
            }
        } catch (e: Exception) { return@withContext "Excepción: ${e.message}" }
    }

    private fun getToolsDefinition(activeAgent: String): JSONArray {
        val tools = JSONArray()
        
        // Solo añadimos herramientas si el agente es "Ventas"
        if (activeAgent == "Ventas") {
            tools.put(JSONObject().apply {
                put("function_declarations", JSONArray().apply {
                    put(JSONObject().apply {
                        put("name", "obtener_inventario")
                        put("description", "Consulta el inventario de productos.")
                    })
                    put(JSONObject().apply {
                        put("name", "calcular_envio")
                        put("description", "Calcula costo de envío.")
                        put("parameters", JSONObject().apply {
                            put("type", "OBJECT")
                            put("properties", JSONObject().apply {
                                put("ubicacion", JSONObject().apply { put("type", "STRING") })
                            })
                            put("required", JSONArray().put("ubicacion"))
                        })
                    })
                })
            })
        }
        // Si fuera otro agente, simplemente retornamos el array vacío
        return tools
    }

    private fun parseResponse(jsonResponse: String): String {
        return try {
            val json = JSONObject(jsonResponse)
            val part = json.getJSONArray("candidates").getJSONObject(0)
                .getJSONObject("content").getJSONArray("parts").getJSONObject(0)
            
            if (part.has("functionCall")) {
                val functionCall = part.getJSONObject("functionCall")
                "FUNCTION_CALL:${functionCall.getString("name")}:${functionCall.getJSONObject("args")}"
            } else {
                part.getString("text")
            }
        } catch (e: Exception) { "Error al interpretar: ${e.message}" }
    }
}
