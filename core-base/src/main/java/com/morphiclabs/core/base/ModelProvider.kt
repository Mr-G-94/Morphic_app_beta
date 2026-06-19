package com.morphiclabs.core.base

interface ModelProvider {
    suspend fun fetchAvailableModels(apiKey: String): List<String>
}
