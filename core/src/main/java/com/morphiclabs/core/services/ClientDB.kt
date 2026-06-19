package com.morphiclabs.core.services

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.content.ContentValues

class ClientDB(private val context: Context) {

    init {
        // Inicializamos tabla de clientes al crear la instancia
        getDb().use { db ->
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS clients (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT UNIQUE,
                    address TEXT,
                    phone TEXT
                )
            """)
        }
    }

    private fun getDb(): SQLiteDatabase {
        val dbFile = context.getDatabasePath("morphic_gateway.db")
        return SQLiteDatabase.openOrCreateDatabase(dbFile, null)
    }

    fun saveClient(name: String, address: String, phone: String) {
        getDb().use { db ->
            val values = ContentValues().apply {
                put("name", name)
                put("address", address)
                put("phone", phone)
            }
            db.insertWithOnConflict("clients", null, values, SQLiteDatabase.CONFLICT_REPLACE)
        }
    }

    fun getClientAddress(name: String): String? {
        getDb().use { db ->
            val cursor = db.query("clients", arrayOf("address"), "name = ?", arrayOf(name), null, null, null)
            return if (cursor.moveToFirst()) {
                val address = cursor.getString(0)
                cursor.close()
                address
            } else {
                cursor.close()
                null
            }
        }
    }
}
