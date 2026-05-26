package com.morphiclabs.data

import com.morphiclabs.core.MessageProcessor
import kotlinx.coroutines.delay

class MiddlewareLocal : MessageProcessor {
    override suspend fun procesarMensaje(texto: String): String {
        delay(1000) // Simula un delay de 1 segundo
        return "$texto procesado localmente"
    }
}
