package com.morphiclabs.app.services

import android.app.*
import android.content.Intent
import android.os.IBinder
import android.util.Log
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
    private val TAG = "MorphicBot"
    
    private lateinit var telegramService: TelegramService
    private val dispatcher = BotDispatcher(AgentRegistry(), inventoryService, clientDB, this)

    override fun onCreate() {
        super.onCreate()
        
        val token = appConfigManager.getApiKey("telegram") ?: ""
        if (token.isNotEmpty()) {
            Log.d(TAG, "Token de Telegram cargado correctamente.")
            telegramService = TelegramService(token)
        } else {
            Log.e(TAG, "Error: Token de Telegram está vacío.")
        }

        val channelId = "morphic_bot_channel"
        val channel = NotificationChannel(channelId, "Bot Service", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Achichincle Activo")
            .setContentText("Agente: ${appConfigManager.getActiveAgent()}")
            .setSmallIcon(android.R.drawable.ic_menu_agenda)
            .build()

        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (::telegramService.isInitialized) {
            Log.d(TAG, "Iniciando servicio de Telegram...")
            serviceScope.launch { listenToTelegram() }
        } else {
            Log.e(TAG, "No se puede iniciar listenToTelegram porque telegramService no está inicializado.")
        }
        return START_STICKY
    }

    private suspend fun listenToTelegram() {
        var lastUpdateId = 0L
        Log.d(TAG, "Bucle de Telegram iniciado.")
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
                                
                                Log.d(TAG, "Mensaje recibido: $text")
                                if (text.isNotEmpty()) {
                                    val response = dispatcher.dispatch("telegram", chatId, text)
                                    telegramService.sendMessage(chatId, response)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error en bucle de Telegram: ${e.message}")
                delay(5000)
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
