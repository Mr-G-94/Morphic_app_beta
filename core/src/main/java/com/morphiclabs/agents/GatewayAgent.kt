package com.morphiclabs.agents

import android.content.Context
import com.morphiclabs.core.base.AgentContract
import com.morphiclabs.core.base.ModelProvider
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

class GatewayAgent(private val context: Context) : AgentContract, ModelProvider {
    private val client = OkHttpClient()
    private val keyManager = KeyManager()
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
        } catch (e: Exception) {
            return@withContext emptyList()
        }
    }

    override suspend fun canHandle(command: String): Boolean = command.isNotEmpty()

    override suspend fun execute(input: String): String = withContext(Dispatchers.IO) {
        val apiKey = keyManager.getApiKey(context, "gemini") ?: return@withContext "Error: API Key no encontrada."
        val modelName = appConfigManager.getModel()
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"

        val jsonBody = JSONObject().apply {
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
                if (!response.isSuccessful) return@withContext "Error (${response.code}): $modelName podría no ser válido."
                return@withContext if (responseData != null) parseResponse(responseData) else "Error: Respuesta vacía."
            }
        } catch (e: Exception) { return@withContext "Excepción: ${e.message}" }
    }

    private fun parseResponse(jsonResponse: String): String {
        return try {
            JSONObject(jsonResponse).getJSONArray("candidates").getJSONObject(0)
                .getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")
        } catch (e: Exception) { "Error al interpretar: ${e.message}" }
    }
}
