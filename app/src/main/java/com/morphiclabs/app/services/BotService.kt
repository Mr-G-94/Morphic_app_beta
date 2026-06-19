package com.morphiclabs.app.services

import android.app.*
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.morphiclabs.core.dispatchers.BotDispatcher
import com.morphiclabs.core.services.InventoryService
import com.morphiclabs.core.services.ClientDB
import com.morphiclabs.core.services.TelegramService
import com.morphiclabs.core.security.AppConfigManager
import com.morphiclabs.di.AgentRegistry
import kotlinx.coroutines.*
import org.json.JSONObject

class BotService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val inventoryService = InventoryService(this)
    private val clientDB = ClientDB(this)
    private val appConfigManager = AppConfigManager(this)
    
    private lateinit var telegramService: TelegramService
    private val dispatcher = BotDispatcher(AgentRegistry(), inventoryService, clientDB)

    override fun onCreate() {
        super.onCreate()
        
        // Obtenemos el token de Telegram guardado previamente en el AppConfigManager
        val token = appConfigManager.getApiKey("telegram") ?: ""
        if (token.isNotEmpty()) {
            telegramService = TelegramService(token)
        }

        val channelId = "morphic_bot_channel"
        val channel = NotificationChannel(channelId, "Bot Service", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Achichincle Activo")
            .setContentText("Conectado a Telegram...")
            .setSmallIcon(android.R.drawable.ic_menu_agenda)
            .build()

        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (::telegramService.isInitialized) {
            serviceScope.launch { listenToTelegram() }
        }
        return START_STICKY
    }

    private suspend fun listenToTelegram() {
        var lastUpdateId = 0L
        while (serviceScope.isActive) {
            try {
                val updatesJson = telegramService.getUpdates(lastUpdateId)
                if (!updatesJson.isNullOrEmpty()) {
                    val json = JSONObject(updatesJson)
                    if (json.getBoolean("ok")) {
                        val updates = json.getJSONArray("result")
                        for (i in 0 until updates.length()) {
                            val update = updates.getJSONObject(i)
                            lastUpdateId = update.getLong("update_id") + 1
                            
                            val message = update.optJSONObject("message")
                            if (message != null) {
                                val chatId = message.getJSONObject("chat").getLong("id").toString()
                                val text = message.optString("text")
                                
                                if (text.isNotEmpty()) {
                                    // Procesamos el mensaje a través de nuestra IA y servicios
                                    val response = dispatcher.dispatch("telegram", chatId, text)
                                    telegramService.sendMessage(chatId, response)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                delay(5000) // Espera antes de reintentar si falla la conexión
            }
            delay(1000)
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
