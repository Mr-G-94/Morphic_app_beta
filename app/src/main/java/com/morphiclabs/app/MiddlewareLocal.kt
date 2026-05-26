package com.morphiclabs.app

import kotlinx.coroutines.delay

class MiddlewareLocal {
    suspend fun procesarMensaje(texto: String): String {
        delay(1000) // Simula un delay de 1 segundo
        return "$texto procesado localmente"
    }
}
