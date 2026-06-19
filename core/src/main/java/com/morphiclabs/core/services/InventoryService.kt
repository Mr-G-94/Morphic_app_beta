package com.morphiclabs.core.services

import android.content.Context
import android.database.sqlite.SQLiteDatabase

class InventoryService(private val context: Context) {

    private fun getDb(): SQLiteDatabase {
        val dbFile = context.getDatabasePath("morphic_gateway.db")
        dbFile.parentFile?.mkdirs()
        return SQLiteDatabase.openOrCreateDatabase(dbFile, null)
    }

    fun getInventory(): String {
        return try {
            getDb().use { db ->
                // Asumimos una tabla 'productos', ajústalo según tu esquema real
                val cursor = db.rawQuery("SELECT name, price, stock FROM products", null)
                val results = mutableListOf<String>()
                
                if (cursor.moveToFirst()) {
                    do {
                        val name = cursor.getString(0)
                        val price = cursor.getString(1)
                        val stock = cursor.getString(2)
                        results.add("$name: $$price (Stock: $stock)")
                    } while (cursor.moveToNext())
                }
                cursor.close()
                
                if (results.isEmpty()) "El inventario está vacío." else results.joinToString("\n")
            }
        } catch (e: Exception) {
            "Error consultando inventario: ${e.message}"
        }
    }
}
