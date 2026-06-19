package com.morphiclabs.core.services

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class TelegramService(private val botToken: String) {
    private val client = OkHttpClient()
    private val baseUrl = "https://api.telegram.org/bot$botToken"

    fun getUpdates(offset: Long): String? {
        val url = "$baseUrl/getUpdates?offset=$offset&timeout=30"
        val request = Request.Builder().url(url).build()
        return client.newCall(request).execute().use { response ->
            if (response.isSuccessful) response.body?.string() else null
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
        client.newCall(request).execute().close()
    }
}
