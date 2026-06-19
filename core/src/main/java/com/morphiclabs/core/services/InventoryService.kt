package com.morphiclabs.core.services

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.content.ContentValues

class InventoryService(private val context: Context) {

    private fun getDb(): SQLiteDatabase {
        val dbFile = context.getDatabasePath("morphic_gateway.db")
        dbFile.parentFile?.mkdirs()
        return SQLiteDatabase.openOrCreateDatabase(dbFile, null)
    }

    fun getInventory(): String {
        return try {
            getDb().use { db ->
                val cursor = db.rawQuery("SELECT name, price, stock FROM products", null)
                val results = mutableListOf<String>()
                if (cursor.moveToFirst()) {
                    do {
                        results.add("${cursor.getString(0)}: $${cursor.getString(1)} (Stock: ${cursor.getInt(2)})")
                    } while (cursor.moveToNext())
                }
                cursor.close()
                if (results.isEmpty()) "El inventario está vacío." else results.joinToString("\n")
            }
        } catch (e: Exception) { "Error: ${e.message}" }
    }

    // Nuevo método para obtener stock individual
    fun getStock(productId: String): Int {
        return try {
            getDb().use { db ->
                val cursor = db.rawQuery("SELECT stock FROM products WHERE name = ?", arrayOf(productId))
                val stock = if (cursor.moveToFirst()) cursor.getInt(0) else 0
                cursor.close()
                stock
            }
        } catch (e: Exception) { 0 }
    }

    // Nuevo método para actualizar stock
    fun updateStock(productId: String, quantityChange: Int): Boolean {
        return try {
            getDb().use { db ->
                // Actualizamos sumando o restando al stock actual
                db.execSQL("UPDATE products SET stock = stock + ? WHERE name = ?", arrayOf(quantityChange, productId))
                true
            }
        } catch (e: Exception) { false }
    }
}
