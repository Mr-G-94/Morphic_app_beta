package com.morphiclabs.app.services

import android.app.*
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.morphiclabs.core.dispatchers.BotDispatcher
import com.morphiclabs.di.AgentRegistry
import kotlinx.coroutines.*

class BotService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val dispatcher = BotDispatcher(AgentRegistry()) // Inicializamos el dispatcher

    override fun onCreate() {
        super.onCreate()
        // Crear notificación necesaria para Foreground Service (Requisito de Android)
        val channelId = "morphic_bot_channel"
        val channel = NotificationChannel(channelId, "Bot Service", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Achichincle Activo")
            .setContentText("Escuchando mensajes en tiempo real...")
            .setSmallIcon(android.R.drawable.ic_menu_agenda)
            .build()

        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Iniciar el bucle de Long Polling
        serviceScope.launch {
            listenToTelegram()
        }
        return START_STICKY // Si Android lo mata por falta de RAM, lo reinicia automáticamente
    }

    private suspend fun listenToTelegram() {
        var lastUpdateId = 0L
        while (isActive) {
            try {
                // Aquí iría tu llamada HTTP a Telegram
                // api.telegram.org/bot<TOKEN>/getUpdates?offset=$lastUpdateId&timeout=30
                
                // Ejemplo conceptual:
                // val updates = telegramApi.getUpdates(lastUpdateId, timeout = 30)
                // for (update in updates) {
                //     dispatcher.dispatch("telegram", update.chatId, update.text)
                //     lastUpdateId = update.id + 1
                // }
            } catch (e: Exception) {
                delay(5000) // Si falla, esperamos 5 seg antes de reintentar
            }
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
