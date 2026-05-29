package com.morphiclabs.agents

import android.content.Context
import com.morphiclabs.core.base.AgentContract
import com.morphiclabs.core.security.KeyManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class GatewayAgent(private val context: Context? = null) : AgentContract {

    private val client = OkHttpClient()
    private val keyManager = KeyManager()

    override suspend fun canHandle(command: String): Boolean {
        return command.trim().isNotEmpty()
    }

    override suspend fun execute(input: String): String = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("model", "gemini-3.5-flash")
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", input)
                    })
                })
            }

            val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
            // Usamos la extensión moderna toRequestBody
            val body = json.toString().toRequestBody(mediaType!!)

            // Intentamos obtener la API Key guardada para el proveedor "gemini"
            val apiKey = context?.let { keyManager.getApiKey(it, "gemini") }

            // Si hay una API Key configurada, podríamos usar el endpoint oficial de Gemini,
            // de lo contrario usamos el endpoint local por defecto.
            val url = if (!apiKey.isNullOrEmpty()) {
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"
            } else {
                "http://127.0.0.1:8000/v1/chat/completions"
            }

            val requestBuilder = Request.Builder()
                .url(url)

            if (!apiKey.isNullOrEmpty()) {
                // Para la API oficial de Gemini, la key se suele pasar como query parameter o header.
                // Aquí preparamos la estructura para soportar headers de autorización o query params.
                requestBuilder.url("$url?key=$apiKey")
            }

            val request = requestBuilder.post(body).build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext "Error al contactar al Gateway: Código de respuesta ${response.code}"
                }
                val responseBody = response.body?.string() ?: return@withContext "Error al contactar al Gateway: Respuesta vacía"

                try {
                    val jsonResponse = JSONObject(responseBody)
                    val choices = jsonResponse.optJSONArray("choices")
                    if (choices != null && choices.length() > 0) {
                        val firstChoice = choices.getJSONObject(0)
                        val message = firstChoice.getJSONObject("message")
                        message.getString("content")
                    } else {
                        // Estructura alternativa (por ejemplo, respuesta directa de Gemini API)
                        val candidates = jsonResponse.optJSONArray("candidates")
                        if (candidates != null && candidates.length() > 0) {
                            val firstCandidate = candidates.getJSONObject(0)
                            val content = firstCandidate.getJSONObject("content")
                            val parts = content.getJSONArray("parts")
                            parts.getJSONObject(0).getString("text")
                        } else {
                            responseBody
                        }
                    }
                } catch (e: Exception) {
                    responseBody
                }
            }
        } catch (e: Exception) {
            "Error al contactar al Gateway: ${e.message}"
        }
    }
}
