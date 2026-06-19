package com.morphiclabs.agents

import android.content.Context
import com.morphiclabs.core.base.AgentContract
import com.morphiclabs.core.security.KeyManager
import com.morphiclabs.core.security.AppConfigManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class GatewayAgent(private val context: Context) : AgentContract {
    private val client = OkHttpClient()
    private val keyManager = KeyManager()
    private val appConfigManager = AppConfigManager(context)

    override suspend fun canHandle(command: String): Boolean {
        return command.isNotEmpty()
    }

    override suspend fun execute(input: String): String = withContext(Dispatchers.IO) {
        val apiKey = keyManager.getApiKey(context, "gemini")
            ?: return@withContext "Error: API Key no encontrada. Configúrala en Ajustes."

        // Ahora obtenemos el modelo dinámicamente desde AppConfigManager
        val modelName = appConfigManager.getModel()

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"

        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", input)
                        })
                    })
                })
            })
        }

        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val responseData = response.body?.string()

                if (!response.isSuccessful) {
                    return@withContext "Error (${response.code}): El modelo '$modelName' podría no ser válido o la API Key es incorrecta."
                }

                if (responseData != null) {
                    return@withContext parseResponse(responseData)
                } else {
                    return@withContext "Error: No se recibió contenido del servidor."
                }
            }
        } catch (e: Exception) {
            return@withContext "Excepción de conexión: ${e.message}"
        }
    }

    private fun parseResponse(jsonResponse: String): String {
        return try {
            val json = JSONObject(jsonResponse)
            json.getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")
        } catch (e: Exception) {
            "Error al interpretar la respuesta del modelo: ${e.message}"
        }
    }
}
