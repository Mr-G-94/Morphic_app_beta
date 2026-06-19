package com.morphiclabs.core.services

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class TelegramService(private val botToken: String) {
    private val client = OkHttpClient()
    private val baseUrl = "https://api.telegram.org/bot$botToken"
    private val TAG = "MorphicBot"

    fun getUpdates(offset: Long): String? {
        val url = "$baseUrl/getUpdates?offset=$offset&timeout=30"
        val request = Request.Builder().url(url).build()
        
        return try {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string()
                if (response.isSuccessful) {
                    Log.d(TAG, "Respuesta recibida: $body")
                    body
                } else {
                    Log.e(TAG, "Error de Telegram: ${response.code} - $body")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción de red en getUpdates: ${e.message}")
            null
        }
    }

    fun sendMessage(chatId: String, text: String) {
        val url = "$baseUrl/sendMessage"
        val json = JSONObject().apply {
            put("chat_id", chatId)
            put("text", text)
        }
        val body = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder().url(url).post(body).build()
        
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Error al enviar mensaje: ${response.code}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al enviar mensaje: ${e.message}")
        }
    }
}
