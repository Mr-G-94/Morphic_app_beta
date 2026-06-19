package com.morphiclabs.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class SystemStats(
    val cpuUsage: Float = 0.45f,
    val memUsage: Float = 0.62f,
    val upload: String = "12.8 Mbps",
    val download: String = "48.3 Mbps",
    val logs: List<String> = listOf("System Boot...", "Gateway Active", "Morphic Agent Ready")
)

class DashboardViewModel : ViewModel() {
    private val _stats = MutableStateFlow(SystemStats())
    val stats: StateFlow<SystemStats> = _stats
    
    // Aquí después inyectaremos los servicios de 'core' para obtener datos reales
}
