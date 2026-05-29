package com.morphiclabs.agents

import com.morphiclabs.core.base.AgentContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONArray
import org.json.JSONObject

class GatewayAgent : AgentContract {

    private val client = OkHttpClient()

    override suspend fun canHandle(command: String): Boolean {
        return command.trim().isNotEmpty()
    }

    override suspend fun execute(input: String): String = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("model", "gemini-1.5-flash")
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", input)
                    })
                })
            }

            val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
            val body = RequestBody.create(mediaType, json.toString())

            val request = Request.Builder()
                .url("http://127.0.0.1:8000/chat/completions")
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext "Error al contactar al Gateway: Código de respuesta ${response.code}"
                }
                val responseBody = response.body?.string() ?: return@withContext "Error al contactar al Gateway: Respuesta vacía"

                try {
                    val jsonResponse = JSONObject(responseBody)
                    val choices = jsonResponse.getJSONArray("choices")
                    val firstChoice = choices.getJSONObject(0)
                    val message = firstChoice.getJSONObject("message")
                    message.getString("content")
                } catch (e: Exception) {
                    // Si no se puede parsear como JSON estándar de OpenAI, devolvemos el cuerpo crudo
                    responseBody
                }
            }
        } catch (e: Exception) {
            "Error al contactar al Gateway: ${e.message}"
        }
    }
}
