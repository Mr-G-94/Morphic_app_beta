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

        if (failureCount.get() >= MAX_FAILURES) {
            val cooldown = 30_000
            if (System.currentTimeMillis() - lastFailureTime < cooldown) {
                return@withContext "Gateway en modo protección. Intenta en unos segundos."
            } else {
                failureCount.set(0)
            }
        }

        try {
            val modelName = "gemini-1.5-flash"

            // ESTRUCTURA CORREGIDA PARA GEMINI API
            val json = JSONObject().apply {
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

            val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
            val body = json.toString().toRequestBody(mediaType!!)
            val apiKey = context?.let { keyManager.getApiKey(it, "gemini") }

            if (apiKey.isNullOrEmpty()) {
                return@withContext "Gateway Error: API Key no configurada."
            }

            val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"
            val request = Request.Builder().url(url).post(body).build()

            client.newCall(request).execute().use { response ->
                val responseBodyString = response.body?.string() ?: ""
                
                if (!response.isSuccessful) {
                    handleFailure()
                    return@withContext "Gateway Error: ${response.code} - ${responseBodyString}"
                }

                failureCount.set(0)
                return@withContext parseResponse(responseBodyString)
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
            // Gemini devuelve la respuesta en candidates -> content -> parts -> text
            if (json.has("candidates")) {
                val candidates = json.getJSONArray("candidates")
                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.getJSONObject("content")
                val parts = content.getJSONArray("parts")
                parts.getJSONObject(0).getString("text")
            } else {
                body
            }
        } catch (e: Exception) {
            body
        }
    }
}
