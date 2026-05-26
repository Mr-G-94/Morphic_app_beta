package com.morphiclabs.core

interface MessageProcessor {
    suspend fun procesarMensaje(texto: String): String
}
