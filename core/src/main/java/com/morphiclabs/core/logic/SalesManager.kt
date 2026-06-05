package com.morphiclabs.core.logic

import com.morphiclabs.core.services.InventoryService

class SalesManager(private val inventoryService: InventoryService) {

    suspend fun processOrder(productId: String, quantity: Int): String {
        val stock = inventoryService.getStock(productId)
        
        if (stock >= quantity) {
            val success = inventoryService.updateStock(productId, -quantity)
            return if (success) {
                "✅ Venta confirmada: $quantity unidades de $productId. Quedan ${inventoryService.getStock(productId)}."
            } else {
                "❌ Error crítico al actualizar inventario."
            }
        } else {
            return "❌ Stock insuficiente. Solo quedan $stock unidades de $productId."
        }
    }
}
