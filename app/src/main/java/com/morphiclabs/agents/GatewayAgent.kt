package com.morphiclabs.agents

import android.content.Context
import com.morphiclabs.core.base.AgentContract
import com.morphiclabs.core.security.KeyManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicInteger

class GatewayAgent(private val context: Context? = null) : AgentContract {
    private val client = OkHttpClient()
    private val keyManager = KeyManager()

    // Circuit Breaker State
    private val failureCount = AtomicInteger(0)
    private val MAX_FAILURES = 3
    private var lastFailureTime = 0L

    override suspend fun canHandle(command: String): Boolean = command.trim().isNotEmpty()

    override suspend fun execute(input: String): String = withContext(Dispatchers.IO) {

        // 1. Verificación de "Circuit Breaker"
        if (failureCount.get() >= MAX_FAILURES) {
            val cooldown = 30_000 // 30 segundos
            if (System.currentTimeMillis() - lastFailureTime < cooldown) {
                return@withContext "Gateway en modo protección (Circuit Open). Intenta en unos segundos."
            } else {
                failureCount.set(0) // Reset después de cooldown
            }
        }

        try {
            // Cambiado a gemini-1.5-flash
            val modelName = "gemini-1.5-flash"
            
            val json = JSONObject().apply {
                put("model", modelName)
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", input)
                    })
                })
            }

            val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
            val body = json.toString().toRequestBody(mediaType!!)
            val apiKey = context?.let { keyManager.getApiKey(it, "gemini") }

            // 2. Selección de Endpoint
            if (apiKey.isNullOrEmpty()) {
                return@withContext "Gateway Error: API Key no configurada."
            }

            val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"
            val request = Request.Builder().url(url).post(body).build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    handleFailure()
                    return@withContext "Gateway Error: ${response.code} - ${response.message}"
                }

                val responseBody = response.body?.string() ?: return@withContext "Respuesta vacía"

                // Si llegamos aquí, éxito: reset de errores
                failureCount.set(0)
                return@withContext parseResponse(responseBody)
            }
        } catch (e: Exception) {
            handleFailure()
            "Gateway Exception: ${e.message}"
        }
    }

    private fun handleFailure() {
        failureCount.incrementAndGet()
        lastFailureTime = System.currentTimeMillis()
    }

    private fun parseResponse(body: String): String {
        return try {
            val json = JSONObject(body)
            when {
                json.has("choices") -> json.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content")
                json.has("candidates") -> {
                    // Parser para la API de Gemini (generativelanguage)
                    val candidates = json.getJSONArray("candidates")
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.getJSONObject("content")
                    val parts = content.getJSONArray("parts")
                    parts.getJSONObject(0).getString("text")
                }
                else -> body
            }
        } catch (e: Exception) {
            body
        }
    }
}
