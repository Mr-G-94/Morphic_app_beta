package com.morphiclabs.core.services

class InventoryService {
    // Simulamos una base de datos en memoria para este Sprint
    // En el futuro, aquí conectarás tu DB (SQL/Room) o Sheets
    private val inventory = mutableMapOf(
        "producto_01" to 10,
        "producto_02" to 5
    )

    fun getStock(productId: String): Int {
        return inventory[productId] ?: 0
    }

    fun updateStock(productId: String, quantityChange: Int): Boolean {
        val currentStock = getStock(productId)
        if (currentStock + quantityChange < 0) return false // No hay suficiente stock
        
        inventory[productId] = currentStock + quantityChange
        return true
    }
}
